package me.szumielxd.portfel.api.configuration;

public interface AbstractKey {
	
	/**
	 * Get path of this config key
	 * 
	 * @return path
	 */
	public String getPath();
	
	/**
	 * Get default value of this config key
	 * 
	 * @return default value
	 */
	public Object getDefault();
	
	/**
	 * Get type of this config value
	 * 
	 * @return type
	 */
	public Class<?> getType();

}
