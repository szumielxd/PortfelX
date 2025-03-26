package me.szumielxd.portfel.bukkit.gui;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Range;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

@SuppressWarnings("deprecation")
public class MainPortfelGui implements AbstractPortfelGui {
	
	private static final ItemStack BACKGROUND;
	
	
	static {
		Material legacyGlass = null;
		try {
			legacyGlass = Material.valueOf("STAINED_GLASS_PANE");
		} catch (Exception e) {
			// fallback to new material
		}
		if (legacyGlass != null) {
			BACKGROUND = new ItemStack(legacyGlass, 1, (byte)15);
		} else {
			BACKGROUND = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		}
		ItemMeta meta = BACKGROUND.getItemMeta();
		meta.setDisplayName("§0");
		BACKGROUND.setItemMeta(meta);
	}
	
	
	private final PortfelBukkitImpl plugin;
	private final Map<Integer, OrderPortfelGui> guis;
	
	
	public MainPortfelGui(@NotNull PortfelBukkitImpl plugin, @NotNull OrderPortfelGui... shops) {
		this.plugin = plugin;
		Range<Integer> range = Range.closed(0, this.getSize()-1);
		this.guis = Stream.of(shops)
				.filter(s -> range.contains(s.getSlot()))
				.collect(Collectors.toMap(s -> s.getSlot(), Function.identity(), (a,b) -> b));
	}
	

	@Override
	public @NotNull MessageDraft getTitle(@NotNull User user, @NotNull Player player) {
		return BukkitLangKey.SHOP_TITLE
				.draft(MainLangKey.MAIN_CURRENCY_FORMAT
						.draft(user.getBalance()));
	}

	@Override
	public int getSize() {
		return this.plugin.getConfiguration().getInt(BukkitConfigKey.SHOP_MENU_ROWS) * 9;
	}

	@Override
	public void onClick(@NotNull Player player, int slot) {
		OrderPortfelGui gui = this.guis.get(slot);
		if (gui != null) {
			User user = this.plugin.getUserManager().getUser(player.getUniqueId());
			if (user != null) {
				PortfelGuiHolder newHolder = new PortfelGuiHolder(this.plugin, gui, user, player);
				newHolder.getGui().setup(player, newHolder.getInventory());
			}
		}
	}

	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		ItemStack[] background = new ItemStack[inventory.getSize()];
		Arrays.fill(background, BACKGROUND);
		var wrapper = BukkitSender.player(plugin, player);
		this.guis.forEach((i, s) -> {
			ItemStack item = s.getIcon();
			ItemMeta meta = item.getItemMeta();
			BukkitUtils.setDisplayName(meta, wrapper, s.getDisplayName());
			BukkitUtils.setLore(meta, wrapper, BukkitLangKey.SHOP_MAIN_LORE.draft(
					s.getDescription().stream()
							.collect(MessageDraft.join(MessageDraft.newline())),
					plugin.getConfiguration().getString(BukkitConfigKey.SHOP_COMMAND_NAME),
					s.getName()));
			item.setItemMeta(meta);
			background[i] = item;
		});
		inventory.setContents(background);
		player.openInventory(inventory);
	}

}
