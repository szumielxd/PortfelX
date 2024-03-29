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
import me.szumielxd.portfel.common.loader.CommonLogger;
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
	 * Official legacy BungeeCord plugin channel. Returns <i>BungeeCord</i>.
	 */
	public static String CHANNEL_LEGACY_BUNGEE = "BungeeCord";
	
	/**
	 * Get proxy server.
	 * 
	 * @return proxy server
	 */
	public @NotNull CommonServer getCommonServer();
	
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
	public @NotNull CommonSender getConsole();
	
	/**
	 * Get plugin's data folder.
	 * 
	 * @return plugin's data folder
	 */
	public @NotNull Path getDataFolder();
	
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
	public @NotNull CommonLogger getLogger();
	
	/**
	 * Log debug message in console.
	 * 
	 * @param message
	 * @param args
	 */
	public default void debug(@NotNull String message, @Nullable Object... args) {
		Objects.requireNonNull(message, "message cannot be null");
		if (this.getConfiguration().getBoolean(ConfigKey.MAIN_DEBUG)) this.getLogger().info("DEBUG: " + String.format(message, args));
	}
	

}
