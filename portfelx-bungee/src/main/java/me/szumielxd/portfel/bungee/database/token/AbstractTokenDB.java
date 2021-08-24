package me.szumielxd.portfel.bungee.database.token;

import java.sql.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.objects.PrizeToken;

public interface AbstractTokenDB {
	
	
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
	 * Load prize related to given token.
	 * 
	 * @implNote Thread unsafe.
	 * @param  token string unique token
	 * @return prize assigned to this token
	 * @throws Exception when something went wrong
	 */
	public @Nullable PrizeToken getToken(@NotNull String token) throws Exception;
	
	/**
	 * Remove token from database. Used to set token as done.
	 * 
	 * @implNote Thread unsafe.
	 * @param token string token
	 * @return true if token was removed from database, otherwise false
	 * @throws Exception when something went wrong
	 */
	public boolean destroyToken(@NotNull String token) throws Exception;
	
	/**
	 * Remove expired tokens from database.
	 * 
	 * @implNote Thread unsafe.
	 * @throws Exception when something went wrong
	 */
	public void cleanupExpired() throws Exception;
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection();
	

}
