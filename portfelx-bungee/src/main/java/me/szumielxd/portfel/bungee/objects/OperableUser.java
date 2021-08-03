package me.szumielxd.portfel.bungee.objects;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.objects.ActionExecutor;
import me.szumielxd.portfel.common.objects.User;

public class OperableUser extends User {
	
	
	private final PortfelBungee plugin;
	

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
	public OperableUser(@NotNull PortfelBungee plugin, @NotNull UUID uuid, @NotNull String name, boolean online, boolean deniedInTop, long balance) {
		super(uuid, name, online, deniedInTop, balance);
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
				this.plugin.getDB().updateUsers(this);
				this.plugin.getDBLogger().logBalanceAdd(this, executor, server, orderName, amount);
				this.plugin.getDB().addBalance(this, amount);
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
				this.plugin.getDB().updateUsers(this);
				this.plugin.getDBLogger().logBalanceTake(this, executor, server, orderName, amount);
				this.plugin.getDB().takeBalance(this, amount);
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
				this.plugin.getDB().updateUsers(this);
				this.plugin.getDBLogger().logBalanceSet(this, executor, server, orderName, newBalance);
				this.plugin.getDB().setBalance(this, newBalance);
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
	 * @param newBalance
	 */
	public void setPlainBalance(long newBalance) {
		this.balance = newBalance;
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
	 * Set whether user should be visible in top balance.
	 * 
	 * @param inTop set to true to allow this user in top
	 * @return A future that will be completed with possible error that occurred during changing inTop flag
	 */
	@Override
	public @NotNull CompletableFuture<Exception> setDeniedInTop(boolean inTop) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.plugin.getDB().setDeniedInTop(this, inTop);
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
				this.plugin.getDB().updateUsers(this);
				return null;
			} catch (Exception e) {
				return e;
			}
		});
	}

}
