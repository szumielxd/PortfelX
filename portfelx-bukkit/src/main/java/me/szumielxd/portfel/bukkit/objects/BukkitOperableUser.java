package me.szumielxd.portfel.bukkit.objects;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager.BalanceUpdateResult;

public class BukkitOperableUser extends User {
	
	
	private final PortfelBukkitImpl plugin;
	private boolean testmode = false;
	
	/**
	 * Real hero and true worker of user instance.
	 * 
	 * @param plugin instance of PortfelBungee
	 * @param uuid unique identifier of user
	 * @param name last known name of user
	 * @param online online status of user
	 * @param deniedInTop true if user can be visible in top
	 * @param balance user's current balance
	 */
	public BukkitOperableUser(@NotNull PortfelBukkitImpl plugin, @NotNull UUID uuid, @NotNull String name, boolean online, boolean deniedInTop, long balance, long minorBalance, @NotNull UUID proxyId, @NotNull String serverName) {
		super(uuid, name, online, deniedInTop, balance, minorBalance);
		this.plugin = plugin;
		this.remoteId = proxyId;
		this.serverName = serverName;
	}
	
	/**
	 * Add specified amount of money to user's balance and log it.
	 * 
	 * @implNote <b>Unsupported for Bukkit instance</b>
	 * @param amount amount of money to add
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when given amount is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> addBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Bukkit instance cannot modify user's balance by itself.");
	}
	
	/**
	 * Take specified amount of money from user's balance and log it.
	 * 
	 * @implNote <b>Unsupported for Bukkit instance</b>
	 * @param amount amount of money to take
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when given amount is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> takeBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Bukkit instance cannot modify user's balance by itself.");
	}
	
	/**
	 * Set specified amount of money as user's balance and log it.
	 * 
	 * @implNote <b>Unsupported for Bukkit instance</b>
	 * @param newBalance new user's balance
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when newBalance is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> setBalance(long newBalance, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Bukkit instance cannot modify user's balance by itself.");
	}
	
	/**
	 * Set specified amount of money as user's balance.
	 * 
	 * @param newBalance new user's balance
	 */
	public void setPlainBalance(long newBalance) {
		this.balance = newBalance;
	}
	
	/**
	 * Set specified amount of money as user's minor balance.
	 * 
	 * @param newBalance new user's balance
	 */
	public void setPlainMinorBalance(long newBalance) {
		this.minorBalance = newBalance;
	}
	
	/**
	 * Set specified visibility state of user's top.
	 * 
	 * @param deniedInTop state of user's visibility in top
	 */
	public void setPlainDeniedInTop(boolean deniedInTop) {
		this.deniedInTop = deniedInTop;
	}
	
	/**
	 * Set user's name.
	 * 
	 * @param name new user's name
	 */
	public void setName(@NotNull String name) {
		this.name = name;
	}
	
	/**
	 * Set user's online status.
	 * 
	 * @param online new online status of player
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	/**
	 * Set user's proxy ID.
	 * 
	 * @param proxyId identifier of user's proxy
	 */
	public void setRemoteId(@NotNull UUID proxyId) {
		this.remoteId = proxyId;
	}
	
	/**
	 * Set user's test-mode status.
	 * 
	 * @param testmode status of user's test-mode
	 */
	public void setTestmode(boolean testmode) {
		this.testmode = testmode;
	}
	
	/**
	 * Toggle user's test-mode status.
	 * 
	 * @return new test-mode status
	 */
	public boolean toggleTestMode() {
		this.testmode = !this.testmode;
		return this.testmode;
	}
	
	/**
	 * Get user's test-mode status.
	 */
	public boolean inTestmode() {
		return this.testmode;
	}
	
	/**
	 * Give minor balance to user.
	 * 
	 * @param amount amount of balance to give
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	public @NotNull CompletableFuture<Boolean> giveMinorBalance(long amount) {
		return CompletableFuture.supplyAsync(() -> {
			Player player = this.plugin.getServer().getPlayer(this.getUniqueId());
			try {
				BalanceUpdateResult result = this.plugin.getChannelManager().requestGiveMinorBalance(player, amount);
				this.minorBalance = result.getNewBalance();
				return result.isSuccess();
			} catch (Exception e) {
				return false;	
			}
		});
	}
	
	/**
	 * Take minor balance from user.
	 * 
	 * @param amount amount of balance to take
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	public @NotNull CompletableFuture<Boolean> takeMinorBalance(long amount) {
		if (this.minorBalance < amount) throw new IllegalArgumentException("`amount` cannot be smaller than user's current minor balance");
		return CompletableFuture.supplyAsync(() -> {
			Player player = this.plugin.getServer().getPlayer(this.getUniqueId());
			try {
				BalanceUpdateResult result = this.plugin.getChannelManager().requestTakeMinorBalance(player, amount);
				this.minorBalance = result.getNewBalance();
				return result.isSuccess();
			} catch (Exception e) {
				return false;	
			}
		});
	}
	
	/**
	 * Set minor balance of user.
	 * 
	 * @param amount amount of balance to set
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	public @NotNull CompletableFuture<Boolean> setMinorBalance(long amount) {
		return CompletableFuture.completedFuture(false); // client instance cannot set minor balance
	}
	
	/**
	 * Set whether user should be visible in top balance.
	 * 
	 * @implNote <b>Unsupported for Bukkit instance</b>
	 * @param inTop set to true to allow this user in top
	 * @return A future that will be completed with possible error that occurred during changing inTop flag
	 */
	@Override
	public @NotNull CompletableFuture<Exception> setDeniedInTop(boolean inTop) {
		throw new UnsupportedOperationException("Bukkit instance cannot modify user's top by itself.");
	}

	/**
	 * Update user's data.
	 * 
	 * @return A future that will be completed with possible error that occurred during update
	 */
	@Override
	public @NotNull CompletableFuture<Exception> update() {
		this.bumpLastUpdate();
		return CompletableFuture.supplyAsync(() -> {
			try {
				Player player = this.plugin.getServer().getPlayer(this.getUniqueId());
				this.plugin.getChannelManager().requestPlayer(player);
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

}
