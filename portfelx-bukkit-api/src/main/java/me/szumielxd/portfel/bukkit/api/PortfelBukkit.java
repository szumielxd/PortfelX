package me.szumielxd.portfel.bukkit.api;

import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.bukkit.api.managers.BukkitTopManager;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.api.managers.IdentifierManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public interface PortfelBukkit extends Portfel {
	
	
	/**
	 * Get channel manager.
	 * 
	 * @return channel manager
	 */
	public @NotNull ChannelManager getChannelManager();
	
	/**
	 * Get identifier manager.
	 * 
	 * @return identifier manager
	 */
	public @NotNull IdentifierManager getIdentifierManager();
	
	/**
	 * Get Bukkit audience implementation.
	 * 
	 * @return audiences
	 */
	public @NotNull BukkitAudiences adventure();
	
	/**
	 * Get bukkit server.
	 * 
	 * @return server
	 */
	public @NotNull Server getServer();
	
	/**
	 * Get top manager.
	 * 
	 * @return top manager
	 */
	@Override
	public @NotNull BukkitTopManager getTopManager();
	

}
