package me.szumielxd.portfel.bukkit.gui;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.objects.User;
import net.kyori.adventure.text.Component;

public class MainPortfelGui implements AbstractPortfelGui {
	
	
	private static ItemStack BACKGROUND;
	
	
	static {
		try { BACKGROUND = new ItemStack(Material.getMaterial("WOOL"), 1, (byte)5); } catch (NullPointerException e) { BACKGROUND = new ItemStack(Material.getMaterial("BLACK_WOOL")); }; {
			ItemMeta meta = BACKGROUND.getItemMeta();
			meta.setDisplayName("");
			BACKGROUND.setItemMeta(meta);
		}
	}
	
	
	private final PortfelBukkit plugin;
	private final Map<Integer, OrderPortfelGui> guis;
	
	
	public MainPortfelGui(PortfelBukkit plugin, OrderPortfelGui... shops) {
		this.plugin = plugin;
		Range<Integer> range = Range.between(0, this.getSize()-1);
		this.guis = Stream.of(shops).filter(s -> range.contains(s.getSlot())).collect(Collectors.toMap(s -> s.getSlot(), Function.identity(), (a,b) -> b));
	}
	

	@Override
	public @NotNull Component getTitle(User user) {
		Player player = this.plugin.getServer().getPlayer(user.getUniqueId());
		Lang lang = player != null ? Lang.get(new BukkitPlayer(this.plugin, player)) : Lang.def();
		return lang.translateComponent(LangKey.SHOP_TITLE.component(DARK_PURPLE, Sets.newHashSet(BOLD), LangKey.SHOP_CURRENCYFORMAT.component(AQUA, Sets.newHashSet(BOLD), Component.text(user.getBalance()))));
	}

	@Override
	public int getSize() {
		return 45;
	}

	@Override
	public void onClick(@NotNull Player player, int slot) {
		OrderPortfelGui gui = this.guis.get(slot);
		if (gui != null) {
			User user = this.plugin.getUserManager().getUser(player.getUniqueId());
			if (user != null) {
				PortfelGuiHolder newHolder = new PortfelGuiHolder(this.plugin, gui, user);
				newHolder.getGui().setup(player, newHolder.getInventory());
			}
		}
	}

	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		ItemStack[] background = new ItemStack[inventory.getSize()];
		Arrays.fill(background, BACKGROUND);
		this.guis.forEach((i, s) -> background[i] = s.getIcon());
		inventory.setContents(background);
	}

}
