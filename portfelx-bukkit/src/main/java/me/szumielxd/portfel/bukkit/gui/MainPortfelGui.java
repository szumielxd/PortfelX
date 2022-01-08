package me.szumielxd.portfel.bukkit.gui;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class MainPortfelGui implements AbstractPortfelGui {
	
	
	private static ItemStack BACKGROUND;
	
	
	static {
		try { BACKGROUND = new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (byte)15); } catch (NullPointerException e) { BACKGROUND = new ItemStack(Material.getMaterial("BLACK_STAINED_GLASS_PANE")); }; {
			ItemMeta meta = BACKGROUND.getItemMeta();
			meta.setDisplayName("§0");
			BACKGROUND.setItemMeta(meta);
		}
	}
	
	
	private final PortfelBukkitImpl plugin;
	private final Map<Integer, OrderPortfelGui> guis;
	
	
	public MainPortfelGui(PortfelBukkitImpl plugin, OrderPortfelGui... shops) {
		this.plugin = plugin;
		Range<Integer> range = Range.between(0, this.getSize()-1);
		this.guis = Stream.of(shops).filter(s -> range.contains(s.getSlot())).collect(Collectors.toMap(s -> s.getSlot(), Function.identity(), (a,b) -> b));
	}
	

	@Override
	public @NotNull Component getTitle(User user) {
		Player player = this.plugin.getServer().getPlayer(user.getUniqueId());
		Lang lang = player != null ? Lang.get(BukkitPlayer.get(this.plugin, player)) : Lang.def();
		return lang.translateComponent(LangKey.SHOP_TITLE.component(DARK_PURPLE, Sets.newHashSet(BOLD), LangKey.MAIN_CURRENCY_FORMAT.component(AQUA, Sets.newHashSet(BOLD), Component.text(user.getBalance()))));
	}

	@Override
	public int getSize() {
		return this.plugin.getConfiguration().getInt(BukkitConfigKey.SHOP_MENU_ROWS)*9;
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
		this.guis.forEach((i, s) -> {
			ItemStack item = s.getIcon(); {
				ItemMeta meta = item.getItemMeta();
				BukkitUtils.setDisplayName(meta, MiscUtils.parseComponent(s.getDisplayName()));
				List<Component> lore = new ArrayList<>();
				lore.addAll(s.getDescription().stream().map(MiscUtils::parseComponent).map(l -> l.colorIfAbsent(GRAY)).collect(Collectors.toList()));
				lore.add(Component.empty());
				lore.add(Component.text("/" + this.plugin.getConfiguration().getString(BukkitConfigKey.SHOP_COMMAND_NAME) + " " + s.getName(), AQUA));
				BukkitUtils.setLore(meta, lore);
				item.setItemMeta(meta);
			}
			background[i] = item;
		});
		inventory.setContents(background);
		player.openInventory(inventory);
	}

}
