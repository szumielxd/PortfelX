package me.szumielxd.portfel.bukkit.gui;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.common.objects.User;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PortfelGuiHolder implements InventoryHolder {

	
	private static Method Server_createInventory;
	private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().character('§').build();
	
	private final PortfelBukkit plugin;
	private final Inventory inventory;
	private final AbstractPortfelGui gui;
	
	
	static {
		try {
			Server_createInventory = Server.class.getMethod("createInventory", InventoryHolder.class, Integer.TYPE, Component.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	
	public PortfelGuiHolder(@NotNull PortfelBukkit plugin, @NotNull AbstractPortfelGui gui, User user) {
		this.plugin = plugin;
		this.gui = gui;
		Inventory inv;
		try {
			inv = (Inventory) Server_createInventory.invoke(this.plugin.getServer(), this, this.gui.getSize(), this.gui.getTitle(user));
		} catch (NullPointerException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			inv = this.plugin.getServer().createInventory(this, this.gui.getSize(), legacy.serialize(this.gui.getTitle(user)));
		}
		this.inventory = inv;
	}
	
	
	@Override
	public @NotNull Inventory getInventory() {
		return this.inventory;
	}
	
	
	public @NotNull AbstractPortfelGui getGui() {
		return this.gui;
	}

}
