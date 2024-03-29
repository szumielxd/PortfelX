package me.szumielxd.portfel.proxy.objects;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class ProxyOperableUser extends User {
	
	
	private final @NotNull PortfelProxyImpl plugin;
	/**
	 * Flag an object as changed since last update
	 */
	@Getter private @NotNull boolean minorBalanceChanged = false;
	

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
	public ProxyOperableUser(@NotNull PortfelProxyImpl plugin, @NotNull UUID uuid, @NotNull String name, boolean online, boolean deniedInTop, long balance, long minorBalance) {
		super(uuid, name, online, deniedInTop, balance, minorBalance);
		this.plugin = plugin;
	}
	
	/**
	 * Add specified amount of money to user's balance and log it.
	 * 
	 * @param amount amount of money to add
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when given amount is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> addBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (balance < 0) throw new IllegalArgumentException("amount cannot be lower than 0");
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.plugin.getDatabase().updateUsers(this);
				this.plugin.getTransactionLogger().logBalanceAdd(this, executor, server, orderName, amount);
				this.plugin.getDatabase().addBalance(this, amount);
			} catch (Exception e) {
				return e;
			}
			super.addBalance(amount, executor, server, orderName);
			return null;
		});
	}
	
	/**
	 * Take specified amount of money from user's balance and log it.
	 * 
	 * @param amount amount of money to take
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when given amount is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> takeBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (amount < 0) throw new IllegalArgumentException("amount cannot be lower than 0");
		if (this.balance - amount < 0) throw new IllegalArgumentException("balance cannot be lower than 0");
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.plugin.getDatabase().updateUsers(this);
				this.plugin.getTransactionLogger().logBalanceTake(this, executor, server, orderName, amount);
				this.plugin.getDatabase().takeBalance(this, amount);
			} catch (Exception e) {
				return e;
			}
			super.takeBalance(amount, executor, server, orderName);
			return null;
		});
	}
	
	/**
	 * Set specified amount of money as user's balance and log it.
	 * 
	 * @param newBalance new user's balance
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @return A future that will be completed with possible error that occurred during balance change
	 * @throws IllegalArgumentException when newBalance is smaller than 0
	 */
	@Override
	public @NotNull CompletableFuture<Exception> setBalance(long newBalance, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (newBalance < 0) throw new IllegalArgumentException("newBalance cannot be lower than 0");
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.plugin.getDatabase().updateUsers(this);
				this.plugin.getTransactionLogger().logBalanceSet(this, executor, server, orderName, newBalance);
				this.plugin.getDatabase().setBalance(this, newBalance);
			} catch (Exception e) {
				return e;
			}
			super.setBalance(newBalance, executor, server, orderName);
			return null;
		});
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
	 * @param newBalance new user's minor balance
	 */
	public void setPlainMinorBalance(long newBalance) {
		this.balance = newBalance;
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
	 * Set user's remote server's identifier
	 * 
	 * @param serverId UUID of server
	 * @param serverName name of server
	 */
	public void setRemoteIdAndName(@Nullable UUID serverId, @Nullable String serverName) {
		this.remoteId = serverId;
		this.serverName = serverName;
	}
	
	/**
	 * Give minor balance to user.
	 * 
	 * @param amount amount of balance to give
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	@Override
	public @NotNull CompletableFuture<Boolean> giveMinorBalance(long amount) {
		this.minorBalance += amount;
		this.minorBalanceChanged = true;
		return CompletableFuture.completedFuture(true);
	}
	
	/**
	 * Take minor balance from user.
	 * 
	 * @param amount amount of balance to take
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	@Override
	public @NotNull CompletableFuture<Boolean> takeMinorBalance(long amount) {
		if (this.minorBalance < amount) throw new IllegalArgumentException("`amount` cannot be bigger than minor balance");
		this.minorBalance -= amount;
		this.minorBalanceChanged = true;
		return CompletableFuture.completedFuture(true);
	}
	
	/**
	 * Set minor balance of user.
	 * 
	 * @param amount amount of balance to set
	 * @return A future that will be completed with true if succeeded, otherwise false
	 */
	@Override
	public @NotNull CompletableFuture<Boolean> setMinorBalance(long amount) {
		this.minorBalance = amount;
		this.minorBalanceChanged = true;
		return CompletableFuture.completedFuture(true);
	}
	
	/**
	 * Set whether user should be visible in top balance.
	 * 
	 * @param inTop set to true to allow this user in top
	 * @return A future that will be completed with possible error that occurred during changing inTop flag
	 */
	@Override
	public @NotNull CompletableFuture<Exception> setDeniedInTop(boolean inTop) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.plugin.getDatabase().setDeniedInTop(this, inTop);
				super.setDeniedInTop(inTop);
				return null;
			} catch (Exception e) {
				return e;
			}
		});
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
				this.plugin.getDatabase().updateUsers(this);
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}
	
	public boolean isChanged() {
		return this.minorBalanceChanged;
	}
	
	public boolean isNotChanged() {
		return !this.isChanged();
	}
	
	/**
	 * Set all change flags as false
	 * 
	 * @implNote internal use only
	 */
	public void setUnchanged() {
		this.minorBalanceChanged = false;
	}

}
