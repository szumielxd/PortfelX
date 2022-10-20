package me.szumielxd.portfel.proxy.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

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
	public void setup();
	
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
	 * Get position of given users in balance top. If user doesn't exist in top, then returned position is null.
	 * 
	 * @implNote Thread unsafe.
	 * @param users array of users to get
	 * @return array of positions in the same order as given users array
	 * @throws SQLException when something went wrong
	 */
	public @NotNull Integer[] getTopPos(User... users) throws Exception;
	
	/**
	 * Update given user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param users user to update
	 * @return list of all updated users
	 * @throws Exception when something went wrong
	 */
	public List<ProxyOperableUser> updateUsers(@NotNull ProxyOperableUser... users) throws Exception;
	
	/**
	 * Save user values marked as changed.
	 * 
	 * @implNote Internal use only, users are saved automatically.
	 * @param users users to operate on
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void saveChanges(@NotNull ProxyOperableUser... users) throws Exception;
	
	/**
	 * Add given amount of money to balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to add
	 * @throws Exception when something went wrong
	 */
	public void addBalance(@NotNull ProxyOperableUser user, long amount) throws Exception;
	
	/**
	 * Add given amount of money to minor balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to add
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void addMinorBalance(@NotNull ProxyOperableUser user, long amount) throws Exception;
	
	/**
	 * Take given amount of money from balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to take
	 * @throws Exception when something went wrong
	 */
	public void takeBalance(@NotNull ProxyOperableUser user, long amount) throws Exception;
	
	/**
	 * Take given amount of money from minor balance of specified user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to take
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void takeMinorBalance(@NotNull ProxyOperableUser user, long amount) throws Exception;
	
	/**
	 * Set balance of specified user to given amount.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param balance new balance
	 * @throws Exception when something went wrong
	 */
	public void setBalance(@NotNull ProxyOperableUser user, long balance) throws Exception;
	
	/**
	 * Set minor balance of specified user to given amount.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param balance new balance
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void setMinorBalance(@NotNull ProxyOperableUser user, long balance) throws SQLException;
	
	/**
	 * Set whether user should be visible in balance top.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param deniedInTop true if user can be visible in top
	 * @throws Exception when something went wrong
	 */
	public void setDeniedInTop(@NotNull ProxyOperableUser user, boolean deniedInTop) throws Exception;
	
	/**
	 * Set whether user should be visible in balance top.
	 * 
	 * @implNote Internal use only, try {@link TopManager} instead. Thread unsafe.
	 * @param limit max size of top
	 * @return list of top entries sorted from first to last
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> getTop(int limit) throws Exception;
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection();
	

}
