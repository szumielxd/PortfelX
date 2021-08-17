package me.szumielxd.portfel.api.managers;

import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.User;

public interface UserManager {
	

	/**
	 * Force update for all currently online users.
	 * 
	 * @throws Exception when something went wrong
	 */
	public void updateUsers();
	
	public boolean isInitialized();
	
	public boolean isDead();
	
	public boolean isValid();
	
	/**
	 * Get loaded user from UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return user if is loaded, otherwise null
	 */
	public @Nullable User getUser(@NotNull UUID uuid);
	
	/**
	 * Get loaded user from username.
	 * 
	 * @param username name of user
	 * @return user if is loaded, otherwise null
	 */
	public @Nullable User getUser(@NotNull String username);
	
	/**
	 * Get loaded user or load user assigned to given UUID.
	 * 
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	public @Nullable User getOrLoadUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Get loaded user or load user assigned to given username.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param username name of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	public @Nullable User getOrLoadUser(@NotNull String username) throws Exception;
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @implNote <b>Thread Unsafe</b>
	 * @param uuid unique identifier of user
	 * @return already loaded user or new one if not loaded already
	 * @throws Exception if something went wrong
	 */
	public @NotNull User getOrCreateUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Get unmodifiable list of all currently loaded users.
	 * 
	 * @return list of users
	 */
	public @NotNull Collection<User> getLoadedUsers();
	
	/**
	 * Force update for all listed users.
	 * 
	 * @param users users to update
	 * @throws Exception when something went wrong
	 */
	public void updateUsers(User... users) throws Exception;
	

}
