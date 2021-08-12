package me.szumielxd.portfel.bukkit.gui;

import java.util.Optional;
import java.util.stream.Stream;

public enum ShopType {
	
	UPGRADE,
	NORMAL,
	;
	
	
	public static Optional<ShopType> parseIgnoreCase(String text) {
		return Stream.of(values()).filter(t -> t.name().equalsIgnoreCase(text)).findAny();
	}
	

}
