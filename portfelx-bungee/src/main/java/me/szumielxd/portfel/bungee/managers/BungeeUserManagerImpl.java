package me.szumielxd.portfel.bungee.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeeOperableUser;
import me.szumielxd.portfel.common.managers.UserManagerImpl;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUserManagerImpl extends UserManagerImpl {

	
	private final PortfelBungeeImpl plugin;
	private final Map<UUID, BungeeOperableUser> users;
	
	
	public BungeeUserManagerImpl(PortfelBungeeImpl plugin) {
		this.plugin = plugin;
		this.users = new HashMap<>();
	}
	
	// TODO: loadOrCreate for array of UUIDs
	@Override
	public BungeeUserManagerImpl init() {
		super.init();
		this.plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getUniqueId).toArray(UUID[]::new);
		//this.plugin.getDB();
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
		BungeeOperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (BungeeOperableUser) this.plugin.getDB().loadUser(uuid);
		if (user != null) this.users.put(uuid, user);
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
		BungeeOperableUser user = this.users.values().stream().filter(u -> u.getName().equalsIgnoreCase(username)).findAny().orElse(null);
		if (user != null) return user;
		user = (BungeeOperableUser) this.plugin.getDB().loadUserByName(username, false);
		if (user != null) this.users.put(user.getUniqueId(), user);
		return user;
	}
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull User getOrCreateUser(@NotNull UUID uuid) throws Exception {
		this.validate();
		BungeeOperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (BungeeOperableUser) this.plugin.getDB().loadOrCreateUser(uuid);
		this.users.put(uuid, user);
		return user;
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
		this.plugin.getDB().updateUsers(Stream.of(users).toArray(BungeeOperableUser[]::new));
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
