package me.szumielxd.portfel.bukkit.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
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
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui;
import me.szumielxd.portfel.bukkit.gui.ShopType;
import me.szumielxd.portfel.bukkit.objects.DoneConditionImpl;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class OrdersManager {
	
	
	private final PortfelBukkitImpl plugin;
	private final File ordersFolder;
	private final Map<String, OrderPortfelGui> categories = new HashMap<>();
	
	
	public OrdersManager(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.ordersFolder = new File(this.plugin.getDataFolder(), "orders");
	}
	
	
	public OrdersManager init() {
		// set defaults
		if (!this.ordersFolder.exists()) {
			this.ordersFolder.mkdirs();
			for (String name : Arrays.asList("money.yml", "rankups.yml")) {
				try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("orders/" + name)) {
					Files.copy(is, new File(this.ordersFolder, name).toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Stream.of(this.ordersFolder.listFiles((dir, f) -> f.endsWith(".yml"))).forEach(this::load);
		
		return this;
	}
	
	
	public @Nullable OrderPortfelGui getGui(@NotNull String name) {
		return this.categories.get(name.toLowerCase());
	}
	
	public Set<String> getNames() {
		return this.categories.values().stream().map(OrderPortfelGui::getName).collect(Collectors.toSet());
	}
	
	public Collection<OrderPortfelGui> getOrderGuis() {
		return this.categories.values();
	}
	
	
	private void load(@NotNull File file) {
		YamlFile yml = new YamlFile(file);
		try {
			yml.load();
			String title = Objects.requireNonNull(yml.getString("title", null), "title must be set");
			ShopType type = ShopType.parseIgnoreCase(yml.getString("type", "")).orElse(ShopType.NORMAL);
			ItemStack icon = BukkitUtils.parseItem(yml.getString("icon", "")).orElse(new ItemStack(Material.STONE));
			int rows = yml.getInt("rows", 5);
			int slot = Objects.requireNonNull((Integer)yml.get("slot", null), "slot must be set");
			String name = Objects.requireNonNull(yml.getString("name", null), "name must be set");
			List<String> description = yml.getStringList("description");
			ConfigurationSection section = Objects.requireNonNull(yml.getConfigurationSection("orders"), "orders section must be set");
			List<OrderData> orders = section.getKeys(false).stream().filter(section::isConfigurationSection).map(section::getConfigurationSection).map(this::loadOrder).filter(Objects::nonNull).collect(Collectors.toList());
			String id = file.getName().substring(0, file.getName().length()-4);
			this.categories.put(id.toLowerCase(), new OrderPortfelGui(this.plugin, id, title, slot, rows, name, description, icon, type, orders));
		} catch (NullPointerException | IOException e) {
			this.plugin.getLogger().warn(e, "Cannot load orders category from file %s", file.getName());
		}
	}
	
	
	private @Nullable OrderData loadOrder(@NotNull ConfigurationSection yml) {
		try {
			final Map<Pattern, String> replacements = yml.getKeys(false).parallelStream().filter(s -> s.startsWith("--")).filter(yml::isString).collect(Collectors.toMap(s -> Pattern.compile(String.format("(?<!(?<!\\\\)\\\\)\\{%s\\}", Pattern.quote(s.substring(2)))), yml::getString));
			Function<String, String> replacer = (str) -> {
				Iterator<Map.Entry<Pattern, String>> iter = replacements.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Pattern, String> e = iter.next();
					str = e.getKey().matcher(str).replaceAll(e.getValue());
				}
				return str.replace("\\{", "{").replace("%name%", yml.getName());
			};
			int slot = Objects.requireNonNull((Integer)yml.get("slot", null), "slot must be set");
			int level = yml.getInt("level", 0);
			String name = replacer.apply(Objects.requireNonNull(yml.getString("name", null), "name must be set"));
			List<Component> description = yml.getStringList("description").parallelStream().map(replacer).map(MiscUtils::parseComponent).collect(Collectors.toList());
			List<Component> denyDescription = yml.getStringList("deny-description").parallelStream().map(replacer).map(MiscUtils::parseComponent).collect(Collectors.toList());
			ItemStack icon = BukkitUtils.parseItem(replacer.apply(yml.getString("icon", ""))).orElse(new ItemStack(Material.STONE));
			ItemStack iconBought = BukkitUtils.parseItem(replacer.apply(yml.getString("icon-bought", ""))).orElse(icon);
			ItemStack iconDenied = BukkitUtils.parseItem(replacer.apply(yml.getString("icon-denied", ""))).orElse(icon);
			int price = Objects.requireNonNull((Integer)yml.get("price", null), "price must be set");
			String donePermission = replacer.apply(yml.getString("done-permission", ""));
			
			// parsing done conditions
			List<DoneCondition> doneConditions = yml.getStringList("done-conditions").parallelStream().map(replacer).map(str -> {
				for (DoneCondition.OperationType type : DoneCondition.OperationType.values()) {
					final Matcher match = type.getPattern().matcher(str);
					if (match.matches()) {
						String sign = type.getSign();
						return new DoneConditionImpl(match.group(1).replace("\\" + sign, sign), match.group(3).replace("\\" + sign, sign), type);
					}
				}
				this.plugin.getLogger().warn("Cannot parse done-condition `%s` for order `%s` in category `%s`", str, yml.getName(), yml.getRoot().getName());
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
			
			// parsing deny conditions
			List<DoneCondition> denyConditions = yml.getStringList("deny-conditions").parallelStream().map(replacer).map(str -> {
				for (DoneCondition.OperationType type : DoneCondition.OperationType.values()) {
					final Matcher match = type.getPattern().matcher(str);
					if (match.matches()) {
						String sign = type.getSign();
						return new DoneConditionImpl(match.group(1).replace("\\" + sign, sign), match.group(3).replace("\\" + sign, sign), type);
					}
				}
				this.plugin.getLogger().warn("Cannot parse deny-condition `%s` for order `%s` in category `%s`", str, yml.getName(), yml.getRoot().getName());
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
			
			List<String> broadcast = yml.getStringList("broadcast").parallelStream().map(replacer).collect(Collectors.toList());
			List<String> message = yml.getStringList("message").parallelStream().map(replacer).collect(Collectors.toList());
			List<String> command = yml.getStringList("command").parallelStream().map(replacer).collect(Collectors.toList());
			return new OrderData(yml.getName(), slot, level, MiscUtils.parseComponent(name), description, denyDescription, icon, iconBought, iconDenied, price, donePermission.isEmpty()? null : donePermission, doneConditions, denyConditions, broadcast, message, command);
		} catch (NullPointerException e) {
			this.plugin.getLogger().warn(e, "Cannot load order `%s` from category `%s`", yml.getName(), yml.getRoot().getName());
		}
		return null;
	}
	

}
