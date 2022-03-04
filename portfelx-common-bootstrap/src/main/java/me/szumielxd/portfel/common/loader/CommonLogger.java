package me.szumielxd.portfel.common.loader;

public interface CommonLogger {
	
	
	/**
	 * Log message at the INFO level.
	 * 
	 * @param message the message string to be logged
	 */
	public void info(String message);
	
	
	/**
	 * Log message at the INFO level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void info(String format, Object... args);
	
	
	/**
	 * Log message at the WARN level.
	 * 
	 * @param message the message string to be logged
	 */
	public void warn(String message);
	
	
	/**
	 * Log message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void warn(String format, Object... args);
	
	
	/**
	 * Log message at the SEVERE level.
	 * 
	 * @param message the message string to be logged
	 */
	public void severe(String message);
	
	
	/**
	 * Log message at the SEVERE level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void severe(String format, Object... args);
	

}
