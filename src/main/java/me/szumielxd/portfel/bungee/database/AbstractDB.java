package me.szumielxd.portfel.bungee.database;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.objects.OperableUser;
import me.szumielxd.portfel.common.objects.User;

public interface AbstractDB {
	
	
	/**
	 * Get name of database's type
	 * 
	 * @return database type's name
	 */
	public @NotNull String getDBName();
	
	/**
	 * Setup database connection properties.
	 */
	public void setup(@NotNull PortfelBungee plugin);
	
	/**
	 * Get database connection.
	 * 
	 * @return database connection
	 * @throws Exception when something went wrong
	 */
	public Connection connect() throws Exception;
	
	/**
	 * Check if database is connected.
	 * 
	 * @return true if connection to database is opened
	 */
	public boolean isConnected();
	
	/**
	 * Check if database connection is valid.
	 * 
	 * @return true if connection to database is valid
	 */
	public boolean isValid();
	
	/**
	 * Shutdown database.
	 */
	public void shutdown();
	
	/**
	 * Load User with given username from database.
	 * 
	 * @implNote Thread unsafe.
	 * @param  last known name of user
	 * @return user related to given name or null when username does not exists in database
	 * @throws Exception when something went wrong
	 */
	public @Nullable User loadUserByName(@NotNull String name, boolean strict) throws Exception;
	
	/**
	 * Load User with given UUID from database.
	 * 
	 * @implNote Thread unsafe.
	 * @param uuid unique identifier of user
	 * @return user related to given UUID or null when UUID does not exists in database
	 * @throws Exception when something went wrong
	 */
	public @Nullable User loadUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Load User with given UUID from database or create new one when user does not exists in database.
	 * 
	 * @implNote Thread unsafe.
	 * @param uuid unique identifier of user
	 * @return user related to given UUID
	 * @throws Exception when something went wrong
	 */
	public @NotNull User loadOrCreateUser(@NotNull UUID uuid) throws Exception;
	
	/**
	 * Update given user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param users user to update
	 * @return list of all updated users
	 * @throws Exception when something went wrong
	 */
	public List<OperableUser> updateUsers(@NotNull OperableUser... users) throws Exception;
	
	/**
	 * Add given amount of money to balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to add
	 * @throws Exception when something went wrong
	 */
	public void addBalance(@NotNull OperableUser user, long amount) throws Exception;
	
	/**
	 * Take given amount of money from balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to take
	 * @throws Exception when something went wrong
	 */
	public void takeBalance(@NotNull OperableUser user, long amount) throws Exception;
	
	/**
	 * Set balance of specified user to given amount.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param balance new balance
	 * @throws Exception when something went wrong
	 */
	public void setBalance(@NotNull OperableUser user, long balance) throws Exception;
	
	/**
	 * Set whether user should be visible in balance top.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param deniedInTop true if user can be visible in top
	 * @throws Exception when something went wrong
	 */
	public void setDeniedInTop(@NotNull OperableUser user, boolean deniedInTop) throws Exception;
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection();
	

}
