package me.szumielxd.portfel.api.objects;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

public abstract class User {
	
	
	protected UUID uuid;
	protected String name;
	protected boolean online;
	protected long lastUpdated;
	protected boolean deniedInTop;
	protected long balance;
	
	
	/**
	 * Functionally retarded user representation with only basic functions.
	 * 
	 * @param uuid unique identifier of user
	 * @param name last known name of user
	 * @param online online status of user
	 * @param deniedInTop true if user can be visible in top
	 * @param balance user's current balance
	 */
	public User(@NotNull UUID uuid, @NotNull String name, boolean online, boolean deniedInTop, long balance) {
		this.uuid = uuid;
		this.name = name;
		this.online = online;
		this.deniedInTop = deniedInTop;
		this.balance = balance;
		this.lastUpdated = System.currentTimeMillis();
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
	public @NotNull CompletableFuture<Exception> addBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (balance < 0) throw new IllegalArgumentException("amount cannot be lower than 0");
		this.balance += amount;
		this.bumpLastUpdate();
		return CompletableFuture.completedFuture((Exception)null);
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
	public @NotNull CompletableFuture<Exception> takeBalance(long amount, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (amount < 0) throw new IllegalArgumentException("amount cannot be lower than 0");
		if (this.balance - amount < 0) throw new IllegalArgumentException("balance cannot be lower than 0");
		this.balance -= amount;
		this.bumpLastUpdate();
		return CompletableFuture.completedFuture((Exception)null);
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
	public @NotNull CompletableFuture<Exception> setBalance(long newBalance, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName) throws IllegalArgumentException {
		if (newBalance < 0) throw new IllegalArgumentException("newBalance cannot be lower than 0");
		this.balance = newBalance;
		this.bumpLastUpdate();
		return CompletableFuture.completedFuture((Exception)null);
	}
	
	/**
	 * Get user's balance.
	 * 
	 * @return user's balance
	 */
	public long getBalance() {
		return this.balance;
	}
	
	/**
	 * Get Unix timestamp from date when user was updated last time.
	 * 
	 * @return last update time
	 */
	public long getLastUpdated() {
		return this.lastUpdated;
	}
	
	/**
	 * Get unique identifier (UUID) of User.
	 * 
	 * @return unique id of user
	 */
	public @NotNull UUID getUniqueId() {
		return this.uuid;
	}
	
	/**
	 * Get last known name of user.
	 * 
	 * @return name of user
	 */
	public @NotNull String getName() {
		return this.name;
	}
	
	/**
	 * Get whether user is online.
	 * 
	 * @return true if user is online
	 */
	public boolean isOnline() {
		return this.online;
	}
	
	/**
	 * Get whether user should be visible in top balance.
	 * 
	 * @return true if user can be visible in top
	 */
	public boolean isDeniedInTop() {
		return this.deniedInTop;
	}
	
	/**
	 * Set whether user should be visible in top balance.
	 * 
	 * @param inTop set to true to allow this user in top
	 * @return A future that will be completed with possible error that occurred during changing inTop flag
	 */
	public @NotNull CompletableFuture<Exception> setDeniedInTop(boolean deniedInTop) {
		this.deniedInTop = deniedInTop;
		this.bumpLastUpdate();
		return CompletableFuture.completedFuture((Exception)null);
	}
	
	/**
	 * Update user's data.
	 * 
	 * @return A future that will be completed with possible error that occurred during update
	 */
	public abstract @NotNull CompletableFuture<Exception> update();
	
	protected void bumpLastUpdate() {
		this.lastUpdated = System.currentTimeMillis();
	}
	

}
