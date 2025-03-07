package me.szumielxd.portfel.proxy.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.managers.UserManagerImpl;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.PortfelProxy;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public class ProxyUserManagerImpl<C> extends UserManagerImpl<C> {

	
	private final PortfelProxyImpl<C> plugin;
	private final Map<UUID, ProxyOperableUser> users;
	private @Nullable ExecutedTask usersSaveTask;
	
	
	public ProxyUserManagerImpl(PortfelProxyImpl<C> plugin) {
		this.plugin = plugin;
		this.users = new ConcurrentHashMap<>();
	}
	
	// TODO: loadOrCreate for array of UUIDs
	@Override
	public ProxyUserManagerImpl<C> init() {
		super.init();
		//this.plugin.getCommonServer().getPlayers().stream().map(ProxyPlayer::getUniqueId).toArray(UUID[]::new);
		//this.plugin.getDatabase();
		this.usersSaveTask = this.plugin.getTaskManager().runTaskTimer(() -> {
			try {
				this.saveChangedUsers();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 5, 5, TimeUnit.SECONDS);
		return this;
	}
	
	@Override
	public void killManager() {
		if (this.usersSaveTask != null) this.usersSaveTask.cancel();
		super.killManager();
	}
	
	/**
	 * Get loaded user from UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return user if is loaded, otherwise null
	 */
	@Override
	public @Nullable ProxyOperableUser getUser(@NotNull UUID uuid) {
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
	public @Nullable ProxyOperableUser getUser(@NotNull String username) {
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
	public @Nullable ProxyOperableUser getOrLoadUser(@NotNull UUID uuid) throws Exception {
		this.validate();
		if (!users.containsKey(uuid)) {
			users.putIfAbsent(uuid, plugin.getDatabase().loadUser(uuid));
		}
		return users.get(uuid);
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
	public @Nullable ProxyOperableUser getOrLoadUser(@NotNull String username) throws Exception {
		this.validate();
		ProxyOperableUser user = this.users.values().stream()
				.filter(u -> u.getName().equalsIgnoreCase(username))
				.findAny()
				.orElse(null);
		if (user != null) {
			return user;
		}
		user = plugin.getDatabase().loadUserByName(username, false);
		if (user != null) {
			this.users.put(user.getUniqueId(), user);
		}
		return user;
	}
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param uuid unique identifier of user
	 * @param username last known name of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public @NotNull ProxyOperableUser getOrCreateUser(@NotNull UUID uuid, @NotNull String username) throws Exception {
		this.validate();
		ProxyOperableUser user = this.users.get(uuid);
		if (user != null) {
			return user;
		}
		user = plugin.getDatabase().loadOrCreateUser(uuid, username);
		this.users.put(uuid, user);
		return user;
	}
	
	public @NotNull ProxyOperableUser getOrCreateUser(@NotNull UUID uuid, @NotNull String username, boolean markJoined) throws Exception {
		var user = getOrCreateUser(uuid, username);
		if (markJoined) {
			plugin.getDatabase().bumpLastJoin(user);
		}
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
		// update only users
		this.plugin.getDatabase().updateUsers(Stream.of(users).map(ProxyOperableUser.class::cast)
				.filter(ProxyOperableUser::isNotChanged).toArray(ProxyOperableUser[]::new));
	}
	
	public void saveChangedUsers() throws Exception {
		this.validate();
		this.plugin.getDatabase().saveChanges(
				this.users.values().stream()
						.filter(ProxyOperableUser::isChanged)
						.toArray(ProxyOperableUser[]::new)
		);
	}
	
	/**
	 * Get modifiable list of all currently loaded users.
	 * 
	 * @return original list of users
	 */
	@Override
	protected @NotNull Collection<? extends ProxyOperableUser> getLoadedUsersOrigin() {
		return this.users.values();
	}

	/**
	 * Get plugin.
	 * 
	 * @return plugin
	 */
	@Override
	protected @NotNull PortfelProxy<C> getPlugin() {
		return this.plugin;
	}

}
