package me.szumielxd.portfel.proxy.api.objects;

import org.jetbrains.annotations.NotNull;

public interface ProxyServerConnection<C> extends PluginMessageTarget {
	
	
	public @NotNull ProxyServer<C> getServer();
	

}
