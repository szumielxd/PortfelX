package me.szumielxd.portfel.common.managers;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.api.objects.User;

public abstract class UserManagerImpl implements UserManager {
	
	
	private ExecutedTask userCleanup;
	private ExecutedTask userUpdater;
	private Boolean initialized;
	
	
	/**
	 * Initialize UserManager
	 * 
	 * @implNote Internal use only
	 */
	public UserManagerImpl init() {
		this.userCleanup = this.getPlugin().getTaskManager().runTaskTimerAsynchronously( // clear unused and old data
				() -> this.getLoadedUsersOrigin().removeIf(u -> !u.isOnline() && System.currentTimeMillis() - u.getLastUpdated() > 10000
		), 10, 10, TimeUnit.SECONDS);
		this.userUpdater = this.getPlugin().getTaskManager().runTaskTimerAsynchronously(this::updateUsers, 10, 10, TimeUnit.SECONDS);
		this.initialized = true;
		return this;
	}
	
	/**
	 * Shutdown all operations related to userManager
	 */
	public void killManager() {
		if (this.userCleanup != null) this.userCleanup.cancel();
		if (this.userUpdater != null) this.userUpdater.cancel();
		this.userCleanup = null;
		this.userUpdater = null;
		this.initialized = false;
	}

	/**
	 * Force update for all currently online users.
	 * 
	 * @throws Exception when something went wrong
	 */
	@Override
	public void updateUsers() {
		this.validate();
		try {
			this.updateUsers(this.getLoadedUsers().stream().filter(User::isOnline).toArray(User[]::new));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isInitialized() {
		return this.initialized != null;
	}
	
	@Override
	public boolean isDead() {
		return this.initialized == false;
	}
	
	@Override
	public boolean isValid() {
		return this.initialized == true;
	}
	
	protected void validate() {
		if (this.isDead()) throw new IllegalStateException("Cannot operate on dead UserManager");
		if (!this.isInitialized()) throw new IllegalStateException("UserManager is not initialized");
	}
	
	/**
	 * Get loaded user from UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return user if is loaded, otherwise null
	 */
	@Override
	public abstract @Nullable User getUser(@NotNull UUID uuid);
	
	/**
	 * Get loaded user from username.
	 * 
	 * @param username name of user
	 * @return user if is loaded, otherwise null
	 */
	@Override
	public abstract @Nullable User getUser(@NotNull String username);
	
	/**
	 * Get loaded user or load user assigned to given UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public abstract @Nullable User getOrLoadUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Get loaded user or load user assigned to given username.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param username name of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public abstract @Nullable User getOrLoadUser(@NotNull String username) throws Exception;
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	@Override
	public abstract @NotNull User getOrCreateUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Get unmodifiable list of all currently loaded users.
	 * 
	 * @return list of users
	 */
	@Override
	public abstract @NotNull Collection<User> getLoadedUsers();
	
	/**
	 * Force update for all listed users.
	 * 
	 * @param users users to update
	 * @throws Exception when something went wrong
	 */
	@Override
	public abstract void updateUsers(User... users) throws Exception;
	
	/**
	 * Get modifiable list of all currently loaded users.
	 * 
	 * @return original list of users
	 */
	protected abstract @NotNull Collection<? extends User> getLoadedUsersOrigin();
	
	/**
	 * Get plugin.
	 * 
	 * @return plugin
	 */
	protected abstract @NotNull Portfel getPlugin();
	

}
