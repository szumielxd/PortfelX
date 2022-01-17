package me.szumielxd.portfel.proxy.api.objects;

import org.jetbrains.annotations.NotNull;

public interface PluginMessageTarget {
	
	
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message);
	

}
