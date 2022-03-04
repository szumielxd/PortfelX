package me.szumielxd.portfel.bungee;

import java.util.Objects;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.loader.CommonLogger;

public class BungeeLogger implements CommonLogger {
	
	
	private final @NotNull Logger logger;
	
	
	public BungeeLogger(@NotNull Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger cannot be null.");
	}
	
	
	/**
	 * Log message at the INFO level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void info(String message) {
		this.logger.info(message);
	}
	
	
	/**
	 * Log message at the INFO level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void info(String format, Object... args) {
		this.logger.info(String.format(format, args));
	}
	
	
	/**
	 * Log message at the WARN level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void warn(String message) {
		this.logger.warning(message);
	}
	
	
	/**
	 * Log message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void warn(String format, Object... args) {
		this.logger.warning(String.format(format, args));
	}
	
	
	/**
	 * Log message at the SEVERE level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void severe(String message) {
		this.logger.severe(message);
	}
	
	
	/**
	 * Log message at the SEVERE level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void severe(String format, Object... args) {
		this.logger.severe(String.format(format, args));
	}
	

}
