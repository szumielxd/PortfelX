package me.szumielxd.portfel.bungee.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.objects.OperableUser;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.managers.UserManager;
import me.szumielxd.portfel.common.objects.User;

public class BungeeUserManager extends UserManager {

	
	private final PortfelBungee plugin;
	private final Map<UUID, OperableUser> users;
	
	
	public BungeeUserManager(PortfelBungee plugin) {
		this.plugin = plugin;
		this.users = new HashMap<>();
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
		OperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (OperableUser) this.plugin.getDB().loadUser(uuid);
		this.users.put(uuid, user);
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
	public @Nullable User getOrCreateUser(@NotNull UUID uuid) throws Exception {
		this.validate();
		OperableUser user = this.users.get(uuid);
		if (user != null) return user;
		user = (OperableUser) this.plugin.getDB().loadOrCreateUser(uuid);
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
		Validate.allElementsOfType(Arrays.asList(users), OperableUser.class);
		this.plugin.getDB().updateUsers((OperableUser[]) users);
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
