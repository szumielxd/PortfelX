package me.szumielxd.portfel.common.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonLogger {
	
	
	/**
	 * Log message at the INFO level.
	 * 
	 * @param message the message string to be logged
	 */
	public void info(@NotNull String message);
	
	
	/**
	 * Log message at the INFO level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void info(@NotNull String format, @Nullable Object... args);
	
	
	/**
	 * Log message at the WARN level.
	 * 
	 * @param message the message string to be logged
	 */
	public void warn(@NotNull String message);
	
	
	/**
	 * Log message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void warn(@NotNull String format, @Nullable Object... args);
	
	
	/**
	 * Log throwable and message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void warn(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args);
	
	
	/**
	 * Log message at the SEVERE level.
	 * 
	 * @param message the message string to be logged
	 */
	public void severe(@NotNull String message);
	
	
	/**
	 * Log message at the SEVERE level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void severe(@NotNull String format, @Nullable Object... args);
	

}
