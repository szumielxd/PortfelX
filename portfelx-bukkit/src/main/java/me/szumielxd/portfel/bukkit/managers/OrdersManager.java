package me.szumielxd.portfel.bukkit.managers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.objects.DoneCondition;
import me.szumielxd.portfel.bukkit.api.objects.OrderData;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderActions;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderConditions;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDisplay;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderIcons;
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui;
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui.OrderGuiDisplay;
import me.szumielxd.portfel.bukkit.gui.ShopType;
import me.szumielxd.portfel.bukkit.objects.DoneConditionImpl;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

public class OrdersManager {
	
	
	private final PortfelBukkitImpl plugin;
	private final Path ordersFolder;
	private final Map<String, OrderPortfelGui> categories = new HashMap<>();
	
	
	public OrdersManager(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.ordersFolder = this.plugin.getDataDirectory().resolve("orders");
	}
	
	
	public OrdersManager init() {
		try {
			// set defaults
			if (!Files.exists(this.ordersFolder)) {
				Files.createDirectories(this.ordersFolder);
				Stream.of("money.yml", "rankups.yml")
						.forEach(this::setupOrder);
			}
			
			try (Stream<Path> paths = Files.find(this.ordersFolder, 1, (path, attr) -> path.toString().endsWith(".yml"))) {
				int sum = paths.mapToInt(path -> {
					this.load(path);
					return 1;
				}).sum();
				this.plugin.logger().info("Loaded %d order files!", sum);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	private void setupOrder(String name) {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("orders/" + name)) {
			Files.copy(is, this.ordersFolder.resolve(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public @Nullable OrderPortfelGui getGui(@NotNull String name) {
		return this.categories.get(name.toLowerCase());
	}
	
	public Set<String> getNames() {
		return this.categories.values().stream()
				.map(OrderPortfelGui::getName)
				.collect(Collectors.toSet());
	}
	
	public Collection<OrderPortfelGui> getOrderGuis() {
		return this.categories.values();
	}
	
	
	private void load(@NotNull Path file) {
		this.plugin.logger().info("Loading order file `%s`...", file);
		YamlFile yml = new YamlFile(file.toFile());
		try {
			yml.load();
			String title = Objects.requireNonNull(yml.getString("title", null), "title must be set");
			ShopType type = ShopType.parseIgnoreCase(yml.getString("type", ""))
					.orElse(ShopType.NORMAL);
			ItemStack icon = BukkitUtils.parseItem(yml.getString("icon", ""))
					.orElse(new ItemStack(Material.STONE));
			int rows = yml.getInt("rows", 5);
			int slot = Objects.requireNonNull((Integer) yml.get("slot", null), "slot must be set");
			String name = Objects.requireNonNull(yml.getString("name", null), "name must be set");
			List<String> description = yml.getStringList("description");
			ConfigurationSection section = Objects.requireNonNull(yml.getConfigurationSection("orders"), "orders section must be set");
			List<OrderData> orders = section.getKeys(false).stream()
					.filter(section::isConfigurationSection)
					.map(section::getConfigurationSection)
					.map(this::loadOrder)
					.filter(Objects::nonNull)
					.toList();
			String fileName = file.getFileName().toString();
			String id = fileName.substring(0, fileName.length() - 4);
			this.categories.put(id.toLowerCase(), new OrderPortfelGui(plugin, id, slot, rows,
					new OrderGuiDisplay(
							MessageDraft.plain(title),
							MessageDraft.plain(name),
							description.stream()
									.map(MessageDraft::plain)
									.toList(),
							icon),
					type, orders));
		} catch (NullPointerException | IOException e) {
			this.plugin.logger().warn(e, "Cannot load orders category from file %s", file.getFileName());
		}
	}
	
	
	private @Nullable OrderData loadOrder(@NotNull ConfigurationSection yml) {
		try {
			var replacements = this.buildReplacements(yml);
			var replacer = replacePlaceholders(yml, replacements);
			int slot = Objects.requireNonNull((Integer)yml.get("slot", null), "slot must be set");
			int level = yml.getInt("level", 0);
			String name = replacer.apply(Objects.requireNonNull(yml.getString("name", null), "name must be set"));
			List<String> description = yml.getStringList("description");
			List<String> denyDescription = yml.getStringList("deny-description");
			ItemStack icon = BukkitUtils.parseItem(replacer.apply(yml.getString("icon", "")))
					.orElse(new ItemStack(Material.STONE));
			ItemStack iconBought = BukkitUtils.parseItem(replacer.apply(yml.getString("icon-bought", "")))
					.orElse(icon);
			ItemStack iconDenied = BukkitUtils.parseItem(replacer.apply(yml.getString("icon-denied", "")))
					.orElse(icon);
			int price = Objects.requireNonNull((Integer) yml.get("price", null), "price must be set");
			String donePermission = replacer.apply(yml.getString("done-permission", ""));
			
			// parsing done conditions
			List<DoneCondition> doneConditions = yml.getStringList("done-conditions").parallelStream()
					.map(replacer)
					.map(str -> parseDoneCondition(str, yml))
					.filter(Objects::nonNull)
					.toList();
			
			// parsing deny conditions
			List<DoneCondition> denyConditions = yml.getStringList("deny-conditions").parallelStream()
					.map(replacer)
					.map(str -> parseDoneCondition(str, yml))
					.filter(Objects::nonNull)
					.toList();
			
			List<String> broadcast = yml.getStringList("broadcast").parallelStream()
					.map(replacer)
					.toList();
			List<String> message = yml.getStringList("message").parallelStream()
					.map(replacer)
					.toList();
			List<String> command = yml.getStringList("command").parallelStream()
					.map(replacePlaceholders(yml, replacements))
					.toList();
			return new OrderData(yml.getName(), slot, level,
					new OrderDisplay(name, description, denyDescription, new OrderIcons(icon, iconBought, iconDenied)),
					price,
					new OrderConditions(Optional.of(donePermission).filter(s -> !s.isEmpty()), doneConditions, denyConditions),
					new OrderActions(broadcast, message, command));
		} catch (NullPointerException e) {
			plugin.logger().warn(e, "Cannot load order `%s` from category `%s`", yml.getName(), yml.getRoot().getName());
		}
		return null;
	}
	
	private @NotNull String replacePlaceholders(@NotNull ConfigurationSection yml, Map<Pattern, String> replacements, @NotNull String str) {
		Iterator<Map.Entry<Pattern, String>> iter = replacements.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Pattern, String> e = iter.next();
			str = e.getKey().matcher(str).replaceAll(e.getValue());
		}
		return str.replace("\\{", "{").replace("%name%", yml.getName());
	}
	
	private UnaryOperator<String> replacePlaceholders(@NotNull ConfigurationSection yml, Map<Pattern, String> replacements) {
		return str -> replacePlaceholders(yml, replacements, str);
	}
	
	private @NotNull Map<Pattern, String> buildReplacements(@NotNull ConfigurationSection yml) {
		return yml.getKeys(false).parallelStream()
				.filter(s -> s.startsWith("--"))
				.filter(yml::isString)
				.collect(Collectors.toMap(
						s -> Pattern.compile("(?<!(?<!\\\\)\\\\)\\{%s\\}".formatted(Pattern.quote(s.substring(2)))),
						yml::getString));
	}
	
	private @Nullable DoneCondition parseDenyCondition(@NotNull String str, @NotNull ConfigurationSection yml) {
		return parseCondition(str, "deny-condition", yml);
	}
	
	private @Nullable DoneCondition parseDoneCondition(@NotNull String str, @NotNull ConfigurationSection yml) {
		return parseCondition(str, "done-condition", yml);
	}
	
	private @Nullable DoneCondition parseCondition(@NotNull String str, @NotNull String field, @NotNull ConfigurationSection yml) {
		var result = DoneCondition.tryParse(str);
		if (result.isPresent()) {
			return new DoneConditionImpl(
					result.get().left(),
					result.get().right(),
					result.get().type());
		}
		plugin.logger().warn("Cannot parse %s `%s` for order `%s` in category `%s`", field, str, yml.getName(), yml.getRoot().getName());
		return null;
	}
	

}
