package me.szumielxd.portfel.common;

import java.io.File;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.managers.TaskManager;
import me.szumielxd.portfel.common.managers.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Portfel {
	
	
	
	public static Component PREFIX = LegacyComponentSerializer.legacySection().deserialize("§b§l[§5§lP§b§l]§r §3");
	public static String CHANNEL_USERS = "portfel:userdata";
	public static String CHANNEL_SETUP = "portfel:setup";
	public static String CHANNEL_TRANSACTIONS = "portfel:transactions";
	
	public @NotNull UserManager getUserManager();
	
	public @NotNull TaskManager getTaskManager();
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	/*public AbstractDBLogger getDBLogger();*/
	
	public @NotNull File getDataFolder();
	
	public @NotNull Config getConfiguration();
	
	public @NotNull String getName();
	
	public @NotNull Logger getLogger();
	

}
