package me.szumielxd.portfel.proxy.api;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;
import me.szumielxd.portfel.proxy.api.objects.CommonProxy;

public interface PortfelProxy extends Portfel {
	
	
	/**
	 * Get proxy ID.
	 * 
	 * @return proxy ID
	 */
	public @NotNull UUID getProxyId();
	
	/**
	 * Get access manager.
	 * 
	 * @return access manager
	 */
	public @NotNull AccessManager getAccessManager();
	
	/**
	 * Get proxy server.
	 * 
	 * @return proxy server
	 */
	public @NotNull CommonProxy getProxyServer();

	/**
	 * Get top manager.
	 * 
	 * @return top manager
	 */
	@Override
	public @NotNull ProxyTopManager getTopManager();
	

}
