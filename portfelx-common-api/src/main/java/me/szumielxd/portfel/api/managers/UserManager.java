package me.szumielxd.portfel.api.managers;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
	 */
	public @NotNull CompletableFuture<@Nullable ? extends User> getOrLoadUser(@NotNull UUID uuid);
	
	/**
	 * Get loaded user or load user assigned to given username.
	 * 
	 * @param username name of user
	 * @return already loaded user or new one if not loaded already
	 */
	public @NotNull CompletableFuture<@Nullable ? extends User> getOrLoadUser(@NotNull String username);
	
	/**
	 * Get loaded user or load user assigned to given UUID. When UUID doesn't match any existent user, new one is created.
	 * 
	 * @param uuid unique identifier of user
	 * @param username last known name of user
	 * @return already loaded user or new one if not loaded already
	 */
	public @NotNull CompletableFuture<@NotNull ? extends User> getOrCreateUser(@NotNull UUID uuid, @NotNull String username);
	
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
	 */
	public CompletableFuture<Void> updateUsers(User... users);
	

}
