package me.szumielxd.portfel.api.configuration;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface Config {
	
	
	/**
	 * Initialize configuration
	 * 
	 * @param values to load
	 * @return this object
	 */
	public @NotNull Config init(@NotNull AbstractKey... values);
	
	/**
	 * Get value of given key parsed as string
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public @NotNull String getString(@NotNull AbstractKey key);
	
	/**
	 * Get value of given key parsed as list of strings
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public @NotNull List<String> getStringList(@NotNull AbstractKey key);
	
	/**
	 * Get value of given key parsed as integer
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public int getInt(@NotNull AbstractKey key);
	
	/**
	 * Get value of given key parsed as boolean
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public boolean getBoolean(@NotNull AbstractKey key);
	
	/**
	 * Get value of given key parsed as map of strings
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public @NotNull Map<String, String> getStringMap(@NotNull AbstractKey key);
	

}
