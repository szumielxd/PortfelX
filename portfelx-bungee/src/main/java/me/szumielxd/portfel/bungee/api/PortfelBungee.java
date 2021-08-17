package me.szumielxd.portfel.bungee.api;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.bungee.api.managers.AccessManager;
import me.szumielxd.portfel.bungee.api.managers.BungeeTopManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;

public interface PortfelBungee extends Portfel {
	
	
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
	 * Get Bukkit audience implementation.
	 * 
	 * @return audiences
	 */
	public @NotNull BungeeAudiences adventure();
	
	/**
	 * Get proxy server.
	 * 
	 * @return proxy server
	 */
	public @NotNull ProxyServer getProxy();

	/**
	 * Get top manager.
	 * 
	 * @return top manager
	 */
	@Override
	public @NotNull BungeeTopManager getTopManager();
	

}
