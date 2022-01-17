package me.szumielxd.portfel.proxy.api.objects;

import java.net.SocketAddress;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public interface ProxyServer {
	
	
	public @NotNull Collection<ProxyPlayer> getPlayers();
	
	public @NotNull String getName();
	
	public @NotNull SocketAddress getAddress();
	
	public boolean isRestricted();
	
	public @NotNull String getPermission();
	

}
