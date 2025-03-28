package me.szumielxd.portfel.bukkit.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.objects.BukkitImaginaryUser;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.managers.UserManagerImpl;
import net.kyori.adventure.text.Component;

public class BukkitUserManagerImpl extends UserManagerImpl<Component> {
	
	
	@Getter private final PortfelBukkitImpl plugin;
	private final Map<UUID, User> users;
	
	
	public BukkitUserManagerImpl(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.users = new HashMap<>();
	}
	
	
	/**
	 * Initialize UserManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public @NotNull BukkitUserManagerImpl init() {
		super.init();
		plugin.getChannelManager().setRegisterer(user -> {
			if (user != null) {
				User main = this.users.get(user.getUniqueId());
				switch (main) {
						case BukkitImaginaryUser imaginaryUser -> { // change user class to operable one
							user.setTestmode(imaginaryUser.inTestmode());
			  				users.put(user.getUniqueId(), user);
						}
						case BukkitOperableUser operableUser -> { // update user status
							operableUser.setPlainBalance(user.getBalance());
							operableUser.setPlainDeniedInTop(user.isDeniedInTop());
							operableUser.setOnline(plugin.getServer().getPlayer(user.getUniqueId()) != null);
						}
						default -> users.put(user.getUniqueId(), user); // insert new user
				}
			}
		});
		return this;
	}

	/**
	 * Get loaded user from UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return user if is loaded, otherwise null
	 */
	@Override
	public @Nullable User getUser(@NotNull UUID uuid) {
		this.validate();
		return this.users.get(uuid);
	}
	
	/**
	 * Get loaded user from username.
	 * 
	 * @param username name of user
	 * @return user if is loaded, otherwise null
	 */
	@Override
	public @Nullable User getUser(@NotNull String username) {
		this.validate();
		return this.users.values().stream().filter(u -> u.getName().equalsIgnoreCase(username)).findAny().orElse(null);
	}
	
	/**
	 * Get loaded user or load user assigned to given UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull CompletableFuture<@Nullable ? extends User> getOrLoadUser(@NotNull UUID uuid) {
		this.validate();
		User user = this.users.get(uuid);
		if (user != null) {
			return CompletableFuture.completedFuture(user);
		}
		return loadUser(plugin.getServer().getPlayer(uuid));
	}
	
	/**
	 * Get loaded user or load user assigned to given username.
	 * 
	 * @param username name of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull CompletableFuture<@Nullable ? extends User> getOrLoadUser(@NotNull String username) {
		this.validate();
		var user = this.users.values().stream()
				.filter(u -> u.getName().equalsIgnoreCase(username))
				.findAny()
				.orElse(null);
		if (user != null) {
			return CompletableFuture.completedFuture(user);
		}
		return loadUser(plugin.getServer().getPlayerExact(username));
	}
	
	private @NotNull CompletableFuture<? extends User> loadUser(@Nullable Player player) {
		if (player == null || !player.isOnline()) {
			return CompletableFuture.failedFuture(new IllegalArgumentException("Bukkit implementation only allows online players"));
		}
		return plugin.getChannelManager().requestPlayer(player)
				.whenComplete((u, ex) -> {
					if (ex == null) {
						this.users.put(u.getUniqueId(), u);
					}
				});
	}
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull CompletableFuture<@NotNull User> getOrCreateUser(@NotNull UUID uuid, @NotNull String username) {
		return CompletableFuture.completedFuture(this.users.computeIfAbsent(uuid, key -> new BukkitImaginaryUser(plugin, key)));
	}
	
	/**
	 * Get unmodifiable list of all currently loaded users.
	 * 
	 * @return list of users
	 */
	@Override
	public @NotNull Collection<User> getLoadedUsers() {
		this.validate();
		return Collections.unmodifiableCollection(this.users.values());
	}
	
	/**
	 * Force update for all listed users.
	 * 
	 * @param users users to update
	 * @throws Exception when cannot establish the connection to the database
	 */
	@Override
	public CompletableFuture<Void> updateUsers(User... users) {
		this.validate();
		ChannelManager mgr = this.plugin.getChannelManager();
		Server srv = this.plugin.getServer();
		return CompletableFuture.allOf(Stream.of(users)
				.map(User::getUniqueId)
				.map(srv::getPlayer)
				.filter(Objects::nonNull)
				.map(mgr::requestPlayer)
				.toArray(CompletableFuture[]::new));
	}
	
	/**
	 * Get modifiable list of all currently loaded users.
	 * 
	 * @return original list of users
	 */
	@Override
	protected @NotNull Collection<? extends User> getLoadedUsersOrigin() {
		return this.users.values();
	}

}
