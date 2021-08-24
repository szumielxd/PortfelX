package me.szumielxd.portfel.bungee.api.managers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AccessManager {
	
	
	/**
	 * Check if server is registered.
	 * 
	 * @param serverId identifier of server
	 * @return true if server is registered, otherwise false
	 */
	public boolean canAccess(@NotNull UUID serverId);
	
	/**
	 * Check if server can trigger given order.
	 * 
	 * @param serverId identifier of server
	 * @param order name of order (case-insensitive)
	 * @return true if server can access this order, otherwise false
	 */
	public boolean canAccess(@NotNull UUID serverId, String order);
	
	/**
	 * Get serverId by short-name.
	 * 
	 * @param serverName name of server used to register server (case-insensitive)
	 * @return {@link UUID} of server if given serverName is registered, otherwise null
	 */
	public @Nullable UUID getServerByName(@NotNull String serverName);
	
	/**
	 * Get hash key for given server
	 * 
	 * @param serverId server's identifier
	 * @return hash key string
	 */
	public @Nullable String getHashKey(@NotNull UUID serverId);
	
	/**
	 * Register new server.
	 * 
	 * @param serverId identifier of server
	 * @param serverName user-friendly text representation (case-insensitive)
	 * @param hashKey key used to hash plugin messages
	 * @return false if server is already registered, otherwise true
	 */
	public boolean register(@NotNull UUID serverId, @NotNull String serverName, @NotNull String hashKey);
	
	/**
	 * Unregister server.
	 * 
	 * @param serverId identifier of server
	 * @return false if server is not registered already, otherwise true
	 */
	public boolean unregister(@NotNull UUID serverId);
	
	/**
	 * Add new orderId to list of allowed global orders.
	 * 
	 * @param serverId ID of targeted server
	 * @param order name of the order (case-insensitive)
	 * @return false if serverID doesn't exist, or orderID is already added to allowed orders list
	 */
	public boolean giveAccess(@NotNull UUID serverId, @NotNull String order);
	
	/**
	 * Remove orderId from list of allowed global orders.
	 * 
	 * @param serverId ID of targeted server
	 * @param order name of the order (case-insensitive)
	 * @return false if serverID doesn't exist, or orderID is not in allowed orders list
	 */
	public boolean takeAccess(@NotNull UUID serverId, @NotNull String order);
	
	/**
	 * Get all global orders allowed for given server.
	 * 
	 * @param serverId server to check
	 * @return list of allowed order's names
	 */
	public @Nullable List<String> getAllowedOrders(@NotNull UUID serverId);
	
	/**
	 * Get names of all registered servers accessed by server's ID.
	 * 
	 * @return map of server names and IDs
	 */
	public Map<UUID, String> getServerNames();

}
