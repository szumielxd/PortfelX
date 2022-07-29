package me.szumielxd.portfel.proxy.managers;

import java.util.ArrayList;
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
import me.szumielxd.portfel.common.managers.UserManagerImpl;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public class ProxyUserManagerImpl extends UserManagerImpl {

	
	private final PortfelProxyImpl plugin;
	private final Map<UUID, ProxyOperableUser> users;
	
	
	public ProxyUserManagerImpl(PortfelProxyImpl plugin) {
		this.plugin = plugin;
		this.users = new HashMap<>();
	}
	
	// TODO: loadOrCreate for array of UUIDs
	@Override
	public ProxyUserManagerImpl init() {
		super.init();
		this.plugin.getCommonServer().getPlayers().stream().map(ProxyPlayer::getUniqueId).toArray(UUID[]::new);
		//this.plugin.getDatabase();
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
		ProxyOperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (ProxyOperableUser) this.plugin.getDatabase().loadUser(uuid);
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
		ProxyOperableUser user = this.users.values().stream().filter(u -> u.getName().equalsIgnoreCase(username)).findAny().orElse(null);
		if (user != null) return user;
		user = (ProxyOperableUser) this.plugin.getDatabase().loadUserByName(username, false);
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
		ProxyOperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (ProxyOperableUser) this.plugin.getDatabase().loadOrCreateUser(uuid);
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
		return Collections.unmodifiableCollection(new ArrayList<>(this.users.values()));
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
		this.plugin.getDatabase().updateUsers(Stream.of(users).toArray(ProxyOperableUser[]::new));
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
