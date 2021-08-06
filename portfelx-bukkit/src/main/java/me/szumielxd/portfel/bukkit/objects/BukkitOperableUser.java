package me.szumielxd.portfel.bukkit.objects;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.common.objects.ActionExecutor;
import me.szumielxd.portfel.common.objects.User;

public class BukkitOperableUser extends User {
	
	
	private final PortfelBukkit plugin;
	

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
	public BukkitOperableUser(@NotNull PortfelBukkit plugin, @NotNull UUID uuid, @NotNull String name, boolean online, boolean deniedInTop, long balance) {
		super(uuid, name, online, deniedInTop, balance);
		this.plugin = plugin;
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
