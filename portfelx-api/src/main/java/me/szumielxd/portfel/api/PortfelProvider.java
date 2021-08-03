package me.szumielxd.portfel.api;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Portfel;

public class PortfelProvider {
	
	
	private static Portfel INSTANCE;
	
	
	public static void register(@NotNull Portfel plugin) {
		INSTANCE = plugin;
	}
	
	
	public static void unregister() {
		INSTANCE = null;
	}
	
	
	public static Portfel get() {
		return INSTANCE;
	}
	
	
	private PortfelProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
	

}
