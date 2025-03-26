package me.szumielxd.portfel.bukkit.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.KyoriUtils;
import me.szumielxd.portfel.common.utils.ReflectionUtils;

public class PortfelGuiHolder implements InventoryHolder {

	
	private static @Nullable Method Bukkit_createInventory = ReflectionUtils.tryGetMethod(Bukkit.class, "createInventory", InventoryHolder.class, int.class, KyoriUtils.COMPONENT_CLAZZ);
	
	private final @NotNull PortfelBukkitImpl plugin;
	private final @NotNull Inventory inventory;
	private final @NotNull AbstractPortfelGui gui;
	
	
	public PortfelGuiHolder(@NotNull PortfelBukkitImpl plugin, @NotNull AbstractPortfelGui gui, @NotNull User user, @NotNull Player player) {
		this.plugin = plugin;
		this.gui = gui;
		this.inventory = createInventory(BukkitSender.player(plugin, player), this, gui.getSize(), gui.getTitle(user, player));
	}
	
	
	@Override
	public @NotNull Inventory getInventory() {
		return this.inventory;
	}
	
	
	public @NotNull AbstractPortfelGui getGui() {
		return this.gui;
	}
	
	@SuppressWarnings("deprecation")
	private static @NotNull Inventory createInventory(@NotNull BukkitPlayer wrapper, @NotNull InventoryHolder holder, int size, MessageDraft title) {
		if (Bukkit_createInventory != null) {
			try {
				return (Inventory) Bukkit_createInventory.invoke(null, holder, size, KyoriUtils.toCommonKyori(title.buildComponent(wrapper)));
			} catch (NullPointerException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// fallback to legacy
			}
		}
		return Bukkit.createInventory(holder, size, title.buildLegacy(wrapper));
	}

}
