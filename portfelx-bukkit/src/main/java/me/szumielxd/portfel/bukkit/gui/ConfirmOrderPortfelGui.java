package me.szumielxd.portfel.bukkit.gui;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.bukkit.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.objects.User;
import net.kyori.adventure.text.Component;

public class ConfirmOrderPortfelGui implements AbstractPortfelGui {
	
	
	private static ItemStack ACCEPT;
	private static ItemStack REJECT;
	
	
	static {
		try { ACCEPT = new ItemStack(Material.getMaterial("WOOL"), 1, (byte)5); } catch (NullPointerException e) { ACCEPT = new ItemStack(Material.getMaterial("LIME_WOOL")); };
		try { REJECT = new ItemStack(Material.getMaterial("WOOL"), 1, (byte)14); } catch (NullPointerException e) { REJECT = new ItemStack(Material.getMaterial("RED_WOOL")); };
	}
	
	
	private final PortfelBukkit plugin;
	private final OrderDataOnAir order;
	
	
	public ConfirmOrderPortfelGui(@NotNull PortfelBukkit plugin, OrderDataOnAir order) {
		this.plugin = plugin;
		this.order = order;
	}


	@Override
	public @NotNull Component getTitle(User user) {
		Player player = this.plugin.getServer().getPlayer(user.getUniqueId());
		return (player != null ? Lang.get(BukkitPlayer.get(this.plugin, player)) : Lang.def()).translateComponent(LangKey.SHOP_CONFIRM_TITLE.component(AQUA, LangKey.MAIN_CURRENCY_FORMAT.component(Component.text(this.order.getPrice()))));
	}


	@Override
	public int getSize() {
		return 9;
	}


	@Override
	public void onClick(@NotNull Player player, int slot) {
		if (slot == 2) player.closeInventory(); // reject
		else if (slot == 6) { this.plugin.getTaskManager().runTaskAsynchronously(() -> this.plugin.getChannelManager().requestTransaction(player, this.order)); player.closeInventory(); } // accept
	}


	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.clear();
		Lang lang = player != null ? Lang.get(BukkitPlayer.get(this.plugin, player)) : Lang.def();
		
		ItemStack reject = REJECT.clone(); {
			ItemMeta meta = reject.getItemMeta();
			BukkitUtils.setDisplayName(meta, lang.translateComponent(LangKey.SHOP_CONFIRM_NO_TITLE.component(DARK_RED)));
			BukkitUtils.setLore(meta, Arrays.asList(lang.translateComponent(LangKey.SHOP_CONFIRM_NO_DESCRIPTION.component(GRAY, Component.text(this.order.getPrice(), AQUA), this.order.getDisplay()))));
			reject.setItemMeta(meta);
		}
		
		ItemStack accept = ACCEPT.clone(); {
			ItemMeta meta = accept.getItemMeta();
			BukkitUtils.setDisplayName(meta, lang.translateComponent(LangKey.SHOP_CONFIRM_YES_TITLE.component(GREEN)));
			BukkitUtils.setLore(meta, Arrays.asList(lang.translateComponent(LangKey.SHOP_CONFIRM_YES_DESCRIPTION.component(GRAY, LangKey.MAIN_CURRENCY_FORMAT.component(AQUA, Component.text(this.order.getPrice())), this.order.getDisplay()))));
			accept.setItemMeta(meta);
		}
		
		inventory.setItem(2, reject);
		inventory.setItem(6, accept);
		player.openInventory(inventory);
	}
	

}
