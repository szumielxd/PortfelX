package me.szumielxd.portfel.api;

import org.jetbrains.annotations.NotNull;

public class PortfelProvider {
	
	
	private static Portfel<?> instance;
	
	
	public static void register(@NotNull Portfel<?> plugin) {
		instance = plugin;
	}
	
	
	public static void unregister() {
		instance = null;
	}
	
	
	public static Portfel<?> get() {
		return instance;
	}
	
	
	private PortfelProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
	

}
