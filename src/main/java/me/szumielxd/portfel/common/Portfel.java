package me.szumielxd.portfel.common;

import me.szumielxd.portfel.bungee.database.AbstractDBLogger;
import me.szumielxd.portfel.common.managers.TaskManager;
import me.szumielxd.portfel.common.managers.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Portfel {
	
	
	
	public static Component PREFIX = LegacyComponentSerializer.legacySection().deserialize("§b[§5§lP§b] §3");
	public static String CHANNEL_USERS = "portfel:userdata";
	public static String CHANNEL_SETUP = "portfel:setup";
	public static String CHANNEL_TRANSACTIONS = "portfel:transactions";
	
	public UserManager getUserManager();
	
	public TaskManager getTaskManager();
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	public AbstractDBLogger getDBLogger();
	

}
