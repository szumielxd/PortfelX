package me.szumielxd.portfel.bukkit.api.managers;

import java.util.List;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
	 * Add given amount to user's minor balance.
	 * 
	 * @param player the player
	 * @param amount amount of balance to give
	 * @return {@link BalanceUpdateResult} representation of request result
	 * @throws Exception when something went wrong
	 */
	public @Nullable BalanceUpdateResult requestGiveMinorBalance(@NotNull Player player, int amount) throws Exception;
	
	/**
	 * Remove given amount to user's minor balance.
	 * 
	 * @param player the player
	 * @param amount amount of balance to take
	 * @return {@link BalanceUpdateResult} representation of request result
	 * @throws Exception when something went wrong
	 */
	public @Nullable BalanceUpdateResult requestTakeMinorBalance(@NotNull Player player, int amount) throws Exception;
	
	/**
	 * Cancel user update task if actually pending.
	 * 
	 * @param player player to check
	 */
	public void ensureNotUserUpdating(@NotNull Player player);
	
	/**
	 * Request top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all top entries from given proxy (miscellaneous size)
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> requestTop(@NotNull Player player) throws Exception;
	
	/**
	 * Request minor top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all minor top entries from given proxy (miscellaneous size)
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> requestMinorTop(@NotNull Player player) throws Exception;
	
	/**
	 * Cancel top update task if given player is used as source for this request.
	 * 
	 * @param player player to check
	 */
	public void ensureNotTopRequestSource(@NotNull Player player);
	
	/**
	 * Request transaction for given player with given order.
	 * 
	 * @param player target of transaction
	 * @param order order to complete
	 * @return transaction, with completed state on success
	 */
	public @Nullable Transaction requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order);
	
	
	@AllArgsConstructor
	@Getter
	public class BalanceUpdateResult {
		
		private final boolean success;
		private final long newBalance;
		
	}
	

}
