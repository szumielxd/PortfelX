package me.szumielxd.portfel.bukkit;

import java.util.Arrays;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Config.AbstractKey;

public enum BukkitConfigKey implements AbstractKey {
	
	SERVER_NAME("server.name", "UNKNOWN"),
	SHOP_TERMS_OF_SERVICE("shop.terms-of-service", "http://example.com/terms"),
	SHOP_MENU_BACKGROUND("shop.menu-background", Material.getMaterial("WOOL") != null? "WOOL:15" : "BLACK_WOOL"),
	SHOP_MENU_ROWS("shop.menu-rows", 5),
	SHOP_COMMAND_NAME("shop.command-name", "wallet"),
	SHOP_COMMAND_ALIASES("shop.command-aliases", Arrays.asList("billfold", "bf")),
	;

	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private BukkitConfigKey(@NotNull String path, @NotNull Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.type = defaultValue.getClass();
	}
	
	public String getPath() {
		return this.path;
	}
	
	public Object getDefault() {
		return this.defaultValue;
	}
	
	public Class<?> getType() {
		return this.type;
	}

}
