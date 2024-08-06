package me.szumielxd.portfel.bukkit.gui;

import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import net.kyori.adventure.text.Component;

@SuppressWarnings("deprecation")
public class ConfirmOrderPortfelGui implements AbstractPortfelGui {
	
	
	private static final ItemStack ACCEPT;
	private static final ItemStack REJECT;
	
	
	static {
		Material legacyWool = null;
		try {
			legacyWool = Material.valueOf("WOOL");
		} catch (Exception e) {
			// fallback to new model
		}
		if (legacyWool != null) {
			ACCEPT = new ItemStack(legacyWool, 1, (byte)5);
			REJECT = new ItemStack(legacyWool, 1, (byte)14);
		} else {
			ACCEPT = new ItemStack(Material.getMaterial("LIME_WOOL"));
			REJECT = new ItemStack(Material.getMaterial("RED_WOOL"));
		}
	}
	
	
	private final PortfelBukkitImpl plugin;
	private final OrderDataOnAir order;
	
	
	public ConfirmOrderPortfelGui(@NotNull PortfelBukkitImpl plugin, OrderDataOnAir order) {
		this.plugin = plugin;
		this.order = order;
	}


	@Override
	public @NotNull Component getTitle(@NotNull User user, @NotNull Player player) {
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(player, "player cannot be null");	
		return BukkitLangKey.SHOP_CONFIRM_TITLE
				.draft(BukkitLangKey.MAIN_CURRENCY_FORMAT
						.draft(this.order.getPrice()))
						.buildComponent(BukkitSender.wrap(this.plugin, player));
	}


	@Override
	public int getSize() {
		return 9;
	}


	@Override
	public void onClick(@NotNull Player player, int slot) {
		if (slot == 2) { // reject
			player.closeInventory();
		} else if (slot == 6) { // accept
			this.plugin.getTaskManager().runTaskAsynchronously(
					() -> this.plugin.getChannelManager().requestTransaction(player, this.order));
			player.closeInventory();
			}
	}


	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		Objects.requireNonNull(inventory, "inventory cannot be null");
		Objects.requireNonNull(player, "player cannot be null");
		inventory.clear();
		var wrapper = BukkitSender.wrap(this.plugin, player);
		inventory.setItem(2, buildRejectButton(wrapper));
		inventory.setItem(6, buildAcceptButton(wrapper));
		player.openInventory(inventory);
	}
	
	private ItemStack buildRejectButton(@NotNull BukkitSender wrapper) {
		ItemStack item = REJECT.clone();
		ItemMeta meta = item.getItemMeta();
		BukkitUtils.setDisplayName(meta,
				BukkitLangKey.SHOP_CONFIRM_NO_TITLE
						.draft()
						.buildComponent(wrapper));
		BukkitUtils.setLore(meta, List.of(
				BukkitLangKey.SHOP_CONFIRM_NO_TITLE
						.draft(order.getPrice(), order.getDisplayName())
						.buildComponent(wrapper)));
		item.setItemMeta(meta);
		return item;
	}
	
	private ItemStack buildAcceptButton(@NotNull BukkitSender wrapper) {
		ItemStack item = ACCEPT.clone();
		ItemMeta meta = item.getItemMeta();
		BukkitUtils.setDisplayName(meta,
				BukkitLangKey.SHOP_CONFIRM_YES_TITLE
						.draft()
						.buildComponent(wrapper));
		BukkitUtils.setLore(meta, List.of(
				BukkitLangKey.SHOP_CONFIRM_YES_TITLE
						.draft(order.getPrice(), order.getDisplayName())
						.buildComponent(wrapper)));
		item.setItemMeta(meta);
		return item;
	}
	

}
