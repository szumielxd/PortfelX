package me.szumielxd.portfel.api;

import java.nio.file.Path;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.configuration.Config;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.TopManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.CommonServer;
import me.szumielxd.portfel.api.objects.ComponentMapper;

public interface Portfel<C> {
	
	
	/**
	 * Plugin prefix {@link Component}. Plain format: <i>§b§l[§5§lP§b§l]§r §3</i>.
	 */
	
	/**
	 * Plugin channel used for user-related messages. Returns <i>portfel:userdata</i>.
	 */
	public static final String CHANNEL_INFO = "portfel:data";
	
	/**
	 * Plugin channel used for setup-related messages. Returns <i>portfel:setup</i>.
	 */
	public static final String CHANNEL_SETUP = "portfel:setup";
	
	/**
	 * Plugin channel used for transaction-related messages. Returns <i>portfel:transactions</i>.
	 */
	public static final String CHANNEL_TRANSACTIONS = "portfel:transactions";
	
	public static final String CHANNEL_BUNGEE = "dummy";
	
	/**
	 * Get proxy server.
	 * 
	 * @return proxy server
	 */
	public @NotNull CommonServer<C> getCommonServer();
	
	/**
	 * Get user manager.
	 * 
	 * @return user manager
	 */
	public @NotNull UserManager getUserManager();
	
	/**
	 * Get top manager.
	 * 
	 * @return top manager
	 */
	public @NotNull TopManager getTopManager();
	
	/**
	 * Get task manager.
	 * 
	 * @return task manager
	 */
	public @NotNull TaskManager getTaskManager();
	
	/**
	 * Get Console Sender.
	 * 
	 * @return current console sender
	 */
	public @NotNull CommonSender<C> getConsole();
	
	/**
	 * Get ComponentMapper.
	 * 
	 * @return component mapping utility
	 */
	public @NotNull ComponentMapper<C> getComponentMapper();
	
	/**
	 * Get plugin's data folder.
	 * 
	 * @return plugin's data folder
	 */
	public @NotNull Path getDataDirectory();
	
	/**
	 * Get plugin's configuration.
	 * 
	 * @return plugin's configuration
	 */
	public @NotNull Config getConfiguration();
	
	/**
	 * Get plugin's name.
	 * 
	 * @return plugin's name
	 */
	public @NotNull String getName();
	
	/**
	 * Get plugin's logger.
	 * 
	 * @return plugin's logger
	 */
	public @NotNull CommonLogger logger();
	
	/**
	 * Log debug message in console.
	 * 
	 * @param message
	 * @param args
	 */
	public default void debug(@NotNull String message, @Nullable Object... args) {
		Objects.requireNonNull(message, "message cannot be null");
		if (this.getConfiguration().getBoolean(ConfigKey.MAIN_DEBUG)) this.logger().info("DEBUG: " + String.format(message, args));
	}
	

}
