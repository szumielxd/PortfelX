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
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.objects.OrderData;
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui;
import me.szumielxd.portfel.bukkit.gui.ShopType;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.utils.MiscUtils;

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
		} catch (NullPointerException | InvalidConfigurationException | IOException e) {
			this.plugin.getLogger().log(Level.WARNING, "Cannot load orders category from file "+file.getName(), e);
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
				return str.replace("\\{", "{");
			};
			int slot = Objects.requireNonNull((Integer)yml.get("slot", null), "slot must be set");
			int level = yml.getInt("level", 0);
			String name = replacer.apply(Objects.requireNonNull(yml.getString("name", null), "name must be set"));
			List<String> description = yml.getStringList("description").parallelStream().map(replacer).collect(Collectors.toList());
			ItemStack icon = BukkitUtils.parseItem(replacer.apply(yml.getString("icon", ""))).orElse(new ItemStack(Material.STONE));
			ItemStack iconBought = BukkitUtils.parseItem(replacer.apply(yml.getString("icon-bought", ""))).orElse(new ItemStack(Material.STONE));
			int price = Objects.requireNonNull((Integer)yml.get("price", null), "price must be set");
			String donePermission = yml.getString("done-permission", "");
			List<String> broadcast = yml.getStringList("broadcast").parallelStream().map(replacer).collect(Collectors.toList());
			List<String> message = yml.getStringList("message").parallelStream().map(replacer).collect(Collectors.toList());
			List<String> command = yml.getStringList("command").parallelStream().map(replacer).collect(Collectors.toList());
			return new OrderData(yml.getName(), slot, level, MiscUtils.parseComponent(name), description, icon, iconBought, price, donePermission.isEmpty()? null : donePermission, broadcast, message, command);
		} catch (NullPointerException e) {
			this.plugin.getLogger().log(Level.WARNING, String.format("Cannot load order `%s` from category `%s`", yml.getName(), yml.getRoot().getName()), e);
		}
		return null;
	}
	

}
