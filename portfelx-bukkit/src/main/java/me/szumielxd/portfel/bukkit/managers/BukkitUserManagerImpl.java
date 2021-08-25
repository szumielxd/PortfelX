package me.szumielxd.portfel.bukkit.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.managers.UserManagerImpl;

public class BukkitUserManagerImpl extends UserManagerImpl {
	
	
	private final PortfelBukkitImpl plugin;
	private final Map<UUID, BukkitOperableUser> users;
	
	
	public BukkitUserManagerImpl(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.users = new HashMap<>();
	}
	
	
	/**
	 * Initialize UserManager
	 * 
	 * @implNote Internal use only
	 */
	public @NotNull BukkitUserManagerImpl init() {
		super.init();
		((ChannelManagerImpl)this.plugin.getChannelManager()).setRegisterer(user -> {
			if (user == null) return;
			BukkitOperableUser main = this.users.get(user.getUniqueId());
			if (main != null) {
				main.setPlainBalance(user.getBalance());
				main.setPlainDeniedInTop(user.isDeniedInTop());
				main.setOnline(this.plugin.getServer().getPlayer(user.getUniqueId()) != null);
			} else {
				this.users.put(user.getUniqueId(), user);
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
	 * @implNote <b>Thread Unsafe</b>
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @Nullable User getOrLoadUser(@NotNull UUID uuid) throws Exception {
		this.validate();
		BukkitOperableUser user = this.users.get(uuid);
		if (user != null) return user;
		Player player = this.plugin.getServer().getPlayer(uuid);
		if (player == null) return null;
		user = (BukkitOperableUser) this.plugin.getChannelManager().requestPlayer(player);
		this.users.put(uuid, user);
		return user;
	}
	
	/**
	 * Get loaded user or load user assigned to given username.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param username name of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @Nullable User getOrLoadUser(@NotNull String username) throws Exception {
		this.validate();
		BukkitOperableUser user = this.users.values().stream().filter(u -> u.getName().equalsIgnoreCase(username)).findAny().orElse(null);
		if (user != null) return user;
		Player player = this.plugin.getServer().getPlayerExact(username);
		if (player == null) throw new IllegalArgumentException("Bukkit implementation only allows online players");
		
		user = (BukkitOperableUser) this.plugin.getChannelManager().requestPlayer(player);
		if (user != null) this.users.put(user.getUniqueId(), user);
		return user;
	}
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @implNote <b>Unsupported for Bukkit instance</b>
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull User getOrCreateUser(@NotNull UUID uuid) throws Exception {
		throw new UnsupportedOperationException("Bukkit instance cannot create new users");
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
	public void updateUsers(User... users) throws Exception {
		this.validate();
		ChannelManager mgr = this.plugin.getChannelManager();
		Server srv = this.plugin.getServer();
		Stream.of(users).map(User::getUniqueId).map(srv::getPlayer).filter(Objects::nonNull).forEach(t -> {
			try {
				mgr.requestPlayer(t);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				// silence
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
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

	/**
	 * Get plugin.
	 * 
	 * @return plugin
	 */
	@Override
	protected @NotNull Portfel getPlugin() {
		return this.plugin;
	}

}
