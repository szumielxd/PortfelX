package me.szumielxd.portfel.bukkit.api.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.szumielxd.portfel.api.enums.TransactionStatus;
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
	 */
	public @NotNull CompletableFuture<? extends User> requestPlayer(@NotNull Player player);
	
	/**
	 * Add given amount to user's minor balance.
	 * 
	 * @param player the player
	 * @param amount amount of balance to give
	 * @return {@link BalanceUpdateResult} representation of request result
	 */
	public @Nullable CompletableFuture<BalanceUpdateResult> requestGiveMinorBalance(@NotNull Player player, long amount);
	
	/**
	 * Remove given amount to user's minor balance.
	 * 
	 * @param player the player
	 * @param amount amount of balance to take
	 * @return {@link BalanceUpdateResult} representation of request result
	 */
	public @Nullable CompletableFuture<BalanceUpdateResult> requestTakeMinorBalance(@NotNull Player player, long amount);
	
	/**
	 * Cancel any update task related to given player if actually pending.
	 * 
	 * @param player player to check
	 */
	public void clearAwaitingUpdates(@NotNull Player player);
	
	/**
	 * Request top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all top entries from given proxy (miscellaneous size)
	 */
	public @NotNull CompletableFuture<List<TopEntry>> requestTop(@NotNull Player player);
	
	/**
	 * Request minor top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all minor top entries from given proxy (miscellaneous size)
	 */
	public @NotNull CompletableFuture<List<TopEntry>> requestMinorTop(@NotNull Player player);
	
	/**
	 * Request transaction for given player with given order.
	 * 
	 * @param player target of transaction
	 * @param order order to complete
	 * @return transaction, with completed state on success
	 */
	public @Nullable CompletableFuture<Transaction> requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order);
	
	
	@AllArgsConstructor
	@Getter
	public class BalanceUpdateResult {
		
		private final TransactionStatus success;
		private final long newBalance;
		
	}
	

}
