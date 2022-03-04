package me.szumielxd.portfel.velocity;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import me.szumielxd.portfel.common.loader.CommonLogger;

public class VelocityLogger implements CommonLogger {
	
	
	private final @NotNull Logger logger;
	
	
	public VelocityLogger(@NotNull Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger cannot be null.");
	}
	
	
	/**
	 * Log message at the INFO level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void info(@NotNull String message) {
		this.logger.info(message);
	}
	
	
	/**
	 * Log message at the INFO level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void info(@NotNull String format, @Nullable Object... args) {
		this.logger.info(format, args);
	}
	
	
	/**
	 * Log message at the WARN level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void warn(@NotNull String message) {
		this.logger.warn(message);
	}
	
	
	/**
	 * Log message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void warn(@NotNull String format, @Nullable Object... args) {
		this.logger.warn(format, args);
	}
	
	
	/**
	 * Log throwable and message at the WARN level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	public void warn(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args) {
		this.logger.warn(String.format(format, args), throwable);
	}
	
	
	/**
	 * Log message at the SEVERE level.
	 * 
	 * @param message the message string to be logged
	 */
	@Override
	public void severe(@NotNull String message) {
		this.logger.error(message);
	}
	
	
	/**
	 * Log message at the SEVERE level according to the specified format and arguments.
	 * 
	 * @param format the format string
	 * @param args the arguments
	 */
	@Override
	public void severe(@NotNull String format, @Nullable Object... args) {
		this.logger.error(format, args);
	}
	

}
