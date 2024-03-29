package me.szumielxd.portfel.bukkit.gui;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Range;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.api.objects.OrderData;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.bukkit.utils.PlaceholderUtils;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class OrderPortfelGui implements AbstractPortfelGui {
	
	
	private final PortfelBukkitImpl plugin;
	private final String name;
	private final String title;
	private final int slot;
	private final int size;
	private final String displayName;
	private final List<String> description;
	private final ItemStack icon;
	private final ShopType type;
	private final Map<Integer, OrderData> orders;
	
	
	public OrderPortfelGui(@NotNull PortfelBukkitImpl plugin, @NotNull String name, @NotNull String title, int slot, int rows, @NotNull String displayName, @NotNull List<String> description, ItemStack icon, @NotNull ShopType type, @NotNull List<OrderData> orders) {
		this.plugin = plugin;
		this.name = name;
		this.title = title;
		this.slot = slot;
		this.size = rows*9;
		this.displayName = displayName;
		this.description = Collections.unmodifiableList(description);
		this.icon = icon;
		this.type = type;
		Range<Integer> range = Range.closed(0, this.getSize()-1);
		this.orders = orders.stream().filter(o -> range.contains(o.getSlot())).collect(Collectors.toMap(o -> o.getSlot(), Function.identity(), (a,b) -> b));
	}
	
	
	public @NotNull String getName() {
		return this.name;
	}
	
	@Override
	public @NotNull Component getTitle(@NotNull User user, @NotNull Player player) {
		return PlaceholderUtils.parseComponent(this.title, user, player);
	}
	
	public int getSlot() {
		return this.slot;
	}

	@Override
	public int getSize() {
		return this.size;
	}
	
	public @NotNull String getDisplayName() {
		return this.displayName;
	}
	
	public @NotNull List<String> getDescription() {
		return this.description;
	}
	
	public @NotNull ItemStack getIcon() {
		return this.icon.clone();
	}

	@Override
	public void onClick(@NotNull Player player, int slot) {
		OrderData order = this.orders.get(slot);
		if (order != null) {
			BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
			if (user != null && order.isAvailableToBuy(player) && !order.isDenied(player)) {
				long price = order.getPrice();
				if (this.type.equals(ShopType.UPGRADE)) {
					List<OrderData> orderList = orders.entrySet().stream().map(Entry::getValue).sorted((a,b) -> Integer.compare(a.getLevel(), b.getLevel())).collect(Collectors.toList());
					int index = orderList.indexOf(order);
					if (index < 0) return;
					for (int i = index-1; i >= 0; i--) {
						OrderData o = orderList.get(i);
						if (!o.isAvailableToBuy(player) || o.isDenied(player)) break;
						price += o.getPrice();
					}
				}
				if (price > user.getBalance() && !user.inTestmode()) {
					Optional<Sound> sound = Stream.of(Sound.values()).filter(s -> s.name().equals("ENTITY_VILLAGER_NO")||s.name().equals("VILLAGER_NO")).findAny();
					if (sound.isPresent()) player.playSound(player.getLocation(), sound.get(), 2, 1);
					return;
				}
				PortfelGuiHolder newHolder = new PortfelGuiHolder(this.plugin, new ConfirmOrderPortfelGui(this.plugin, order.onAirWithPrice(price)), user, player);
				newHolder.getGui().setup(player, newHolder.getInventory());
			}
		}
	}

	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.clear();
		List<OrderData> orderList = orders.entrySet().stream().map(Entry::getValue).sorted((a,b) -> Integer.compare(a.getLevel(), b.getLevel())).collect(Collectors.toList());
		Lang lang = Lang.get(BukkitSender.wrap(this.plugin, player));
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user == null) return;
		if (this.type.equals(ShopType.NORMAL)) {
			for (int i = 0; i < orderList.size(); i++) {
				OrderData order = orderList.get(i);
				inventory.setItem(order.getSlot(), this.buildNormalIcon(order, lang, player, user, order.isAvailableToBuy(player), order.isDenied(player)));
			}
		} else if (this.type.equals(ShopType.UPGRADE)) {
			boolean available = true;
			for (int i = orderList.size()-1; i >= 0; i--) {
				OrderData order = orderList.get(i);
				available = available && order.isAvailableToBuy(player);
				inventory.setItem(order.getSlot(), this.buildUpgradeIcon(orderList, i, lang, player, user, available, order.isDenied(player)));
			}
		}
		player.openInventory(inventory);
	}
	
	public @NotNull ShopType getType() {
		return this.type;
	}
	
	private @NotNull ItemStack buildNormalIcon(final @NotNull OrderData order, final @NotNull Lang lang, final @NotNull Player player, final @NotNull User user, final boolean available, final boolean denied) {
		this.plugin.debug("normalItem(%s) %s|%s available: %s, denied: %s", player.getName(), this.getName(), order.getName(), available, denied);
		ItemStack item = (denied ? order.getIconDenied() : available ? order.getIcon() : order.getIconBought()).clone();
		ItemMeta meta = item.getItemMeta();
		BukkitUtils.setDisplayName(meta, PlaceholderUtils.replacePlaceholders(user, player, order.getDisplay()));
		List<Component> lore = new ArrayList<>();
		lore.add(LangKey.SHOP_ORDER_PRICE.component(GRAY, LangKey.MAIN_CURRENCY_FORMAT.component((order.getPrice() <= user.getBalance() ? GREEN : RED), Component.text(order.getPrice()))));
		lore.add(Component.empty());
		lore.add(LangKey.SHOP_ORDER_DESCRIPTION.component(GRAY));
		Component indentation = Component.text("  ", AQUA);
		order.getDescription().stream().map(s -> PlaceholderUtils.replacePlaceholders(user, player, s)).map(indentation::append).forEachOrdered(lore::add);
		if (denied) {
			lore.add(Component.empty());
			order.getDenyDescription().stream().map(s -> PlaceholderUtils.replacePlaceholders(user, player, s)).map(indentation::append).forEachOrdered(lore::add);
			lore.addAll(Arrays.asList(Component.empty(), LangKey.SHOP_ORDER_DENIED.component(RED)));
		} else if (!available) {
			lore.addAll(Arrays.asList(Component.empty(), LangKey.SHOP_ORDER_PURCHASED.component(GREEN)));
		}
		lore.addAll(Arrays.asList(Component.empty(), Component.empty(), LangKey.SHOP_ORDER_TERMS.component(GRAY)));
		lore.add(indentation.append(Component.text(this.plugin.getConfiguration().getString(BukkitConfigKey.SHOP_TERMS_OF_SERVICE))));
		lore.replaceAll(lang::translateComponent);
		BukkitUtils.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	
	private @NotNull ItemStack buildUpgradeIcon(final @NotNull List<OrderData> orders, final int orderIndex, final @NotNull Lang lang, final @NotNull Player player, final @NotNull User user, final boolean available, final boolean denied) {
		this.plugin.debug("upgradeItem(%s) %s|%s available: %s, denied: %s", player.getName(), this.getName(), orders.get(orderIndex).getName(), available, denied);
		OrderData order = orders.get(orderIndex);
		ItemStack item = (denied ? order.getIconDenied() : available ? order.getIcon() : order.getIconBought()).clone();
		ItemMeta meta = item.getItemMeta();
		BukkitUtils.setDisplayName(meta, PlaceholderUtils.replacePlaceholders(user, player, order.getDisplay()));
		
		long fullPrice = order.getPrice();
		long price = order.getPrice();
		List<Component> discounts = new ArrayList<>(); {
			boolean done = false;
			Component prefix = Component.text(" ┗╸ ");
			for (int i = orderIndex-1; i >= 0; i--) {
				OrderData o = orders.get(i);
				if (!o.isAvailableToBuy(player) || o.isDenied(player)) done = true;
				if (!done) {
					price += o.getPrice();
					discounts.add(prefix.color(GRAY).append(Component.text(MiscUtils.firstToUpper(o.getName()), DARK_GRAY)).append(Component.text(" -" + o.getPrice(), DARK_AQUA)));
				} else {
					discounts.add(prefix.color(WHITE).append(Component.text(MiscUtils.firstToUpper(o.getName()), GRAY)).append(Component.text(" -" + o.getPrice(), AQUA)));
				}
				fullPrice += o.getPrice();
				prefix = Component.text(" ┣╸ ");
			}
			Collections.reverse(discounts);
		}
		
		List<Component> lore = new ArrayList<>();
		lore.add(LangKey.SHOP_ORDER_PRICE.component(GRAY, LangKey.MAIN_CURRENCY_FORMAT.component((order.getPrice() <= price? GREEN : RED), Component.text(available ? price : fullPrice))));
		if (available && !denied) lore.addAll(discounts);
		lore.add(Component.empty());
		lore.add(LangKey.SHOP_ORDER_DESCRIPTION.component(GRAY));
		Component indentation = Component.text("  ", AQUA);
		order.getDescription().stream().map(s -> PlaceholderUtils.replacePlaceholders(user, player, s)).map(indentation::append).forEachOrdered(lore::add);
		if (denied) {
			lore.add(Component.empty());
			order.getDenyDescription().stream().map(s -> PlaceholderUtils.replacePlaceholders(user, player, s)).map(indentation::append).forEachOrdered(lore::add);
			lore.addAll(Arrays.asList(Component.empty(), LangKey.SHOP_ORDER_DENIED.component(RED)));
		} else if (!available) {
			lore.addAll(Arrays.asList(Component.empty(), LangKey.SHOP_ORDER_PURCHASED.component(GREEN)));
		}
		lore.addAll(Arrays.asList(Component.empty(), Component.empty(), LangKey.SHOP_ORDER_TERMS.component(GRAY)));
		lore.add(indentation.append(Component.text(this.plugin.getConfiguration().getString(BukkitConfigKey.SHOP_TERMS_OF_SERVICE))));
		lore.replaceAll(lang::translateComponent);
		BukkitUtils.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	

}
