package me.szumielxd.portfel.proxy.api.objects;

import java.util.Collection;
import java.util.Optional;

import me.szumielxd.portfel.api.objects.CommonPlayer;

public interface ProxyPlayer extends CommonPlayer, ProxySender, PluginMessageTarget {

	/**
	 * Get all groups assigned to player.
	 * 
	 * @return connection of all player's group names
	 */
	public Collection<String> getGroups();
	
	/**
	 * Get player's client protocol number.
	 * 
	 * @return integer representation of player's client version
	 */
	public int getVersion();
	
	/**
	 * Check if player's client is modded.
	 * 
	 * @return true if modded, false otherwise
	 */
	public boolean isModded();
	
	/**
	 * Check if player is already connected to proxy.
	 * 
	 * @return true if is connected, false otherwise
	 */
	public boolean isConnected();
	
	/**
	 * Get player's server.
	 * 
	 * @return An Optional containing player's actual server, or empty Optional if player isn't actually connected to any server
	 */
	public Optional<ProxyServerConnection> getServer();

}
