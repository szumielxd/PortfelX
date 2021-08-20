package me.szumielxd.portfel.api;

import java.io.File;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.TopManager;
import me.szumielxd.portfel.api.managers.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Portfel {
	
	
	/**
	 * Plugin prefix {@link Component}. Plain format: <i>§b§l[§5§lP§b§l]§r §3</i>.
	 */
	public static Component PREFIX = LegacyComponentSerializer.legacySection().deserialize("§b§l[§5§lP§b§l]§r §3");
	
	/**
	 * Plugin channel used for user-related messages. Returns <i>portfel:userdata</i>.
	 */
	public static String CHANNEL_USERS = "portfel:userdata";
	
	/**
	 * Plugin channel used for setup-related messages. Returns <i>portfel:setup</i>.
	 */
	public static String CHANNEL_SETUP = "portfel:setup";
	
	/**
	 * Plugin channel used for transaction-related messages. Returns <i>portfel:transactions</i>.
	 */
	public static String CHANNEL_TRANSACTIONS = "portfel:transactions";
	
	/**
	 * Official BungeeCord plugin channel. Returns <i>bungeecord:main</i>.
	 */
	public static String CHANNEL_BUNGEE = "bungeecord:main";
	
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
	 * Get plugin's data folder.
	 * 
	 * @return plugin's data folder
	 */
	public @NotNull File getDataFolder();
	
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
	public @NotNull Logger getLogger();
	

}
