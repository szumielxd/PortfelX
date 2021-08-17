package me.szumielxd.portfel.bukkit.api.managers;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IdentifierManager {
	
	
	/**
	 * Initialize IdentifierManager
	 * 
	 * @implNote Internal use only
	 */
	public IdentifierManager init();
	
	/**
	 * Check whether given proxy Id is already registered on server
	 * 
	 * @param proxyId UUID of proxy
	 * @return true if proxy is registered, otherwise false
	 */
	public boolean isValid(@NotNull UUID proxyId);
	
	/**
	 * Get complementary server ID for given proxy Id
	 * 
	 * @param proxyId UUID of proxy
	 * @return server ID, or null
	 */
	public @Nullable UUID getComplementary(@NotNull UUID proxyId);
	
	/**
	 * Register new proxy-server pair
	 * 
	 * @param proxyId UUID of proxy
	 * @param serverId UUID of server
	 * @return true if pair was successfully registered, otherwise false
	 */
	public boolean register(UUID proxyId, UUID serverId);
	

}
