package me.szumielxd.portfel.bungee;

import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.database.AbstractDBLogger;
import me.szumielxd.portfel.bungee.database.hikari.MariaDB;
import me.szumielxd.portfel.bungee.database.hikari.logging.HikariDBLogger;
import me.szumielxd.portfel.bungee.managers.BungeeTaskManager;
import me.szumielxd.portfel.bungee.managers.BungeeUserManager;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.managers.TaskManager;
import me.szumielxd.portfel.common.managers.UserManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;

public class PortfelBungee extends Plugin implements Portfel {
	
	
	private BungeeAudiences adventure;
	private TaskManager taskManager;
	private UserManager userManager;
	private AbstractDB database;
	private AbstractDBLogger transactionLogger;
	
	
	@Override
	public void onEnable() {
		this.adventure = BungeeAudiences.create(this);
		this.taskManager = new BungeeTaskManager(this);
		this.database = new MariaDB(this);
		this.transactionLogger = new HikariDBLogger(this).init();
		this.userManager = new BungeeUserManager(this).init();
	}
	
	
	@Override
	public void onDisable() {
		this.userManager.killManager();
		this.transactionLogger.killLogger();
		this.database.shutdown();
		this.taskManager.cancelAll();
	}
	
	
	public AbstractDB getDB() {
		return this.database;
	}


	@Override
	public UserManager getUserManager() {
		return this.userManager;
	}
	
	
	@Override
	public TaskManager getTaskManager() {
		return this.taskManager;
	}
	
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	@Override
	public AbstractDBLogger getDBLogger() {
		return this.transactionLogger;
	}
	
	public BungeeAudiences getAdventure() {
		return this.adventure;
	}
	

}
