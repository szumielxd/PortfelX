package me.szumielxd.portfel.bukkit.api.managers;

import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;

public interface ChannelManager {
	
	
	/**
	 * Fetch actual wallet data of given player from proxy.
	 * 
	 * @param player the player
	 * @return {@link User} representation of given player
	 * @throws Exception when something went wrong
	 */
	public @NotNull User requestPlayer(@NotNull Player player) throws Exception;
	
	/**
	 * Request top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all top entries from given proxy (miscellaneous size)
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> requestTop(@NotNull Player player) throws Exception;
	
	/**
	 * Request transaction for given player with given order.
	 * 
	 * @param player target of transaction
	 * @param order order to complete
	 * @return transaction, with completed state on success
	 */
	public @NotNull Transaction requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order);
	

}
