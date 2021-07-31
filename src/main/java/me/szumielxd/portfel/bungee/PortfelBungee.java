package me.szumielxd.portfel.bungee;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import me.szumielxd.portfel.bungee.commands.MainCommand;
import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.database.AbstractDBLogger;
import me.szumielxd.portfel.bungee.database.hikari.MariaDB;
import me.szumielxd.portfel.bungee.database.hikari.logging.HikariDBLogger;
import me.szumielxd.portfel.bungee.listeners.ChannelListener;
import me.szumielxd.portfel.bungee.listeners.UserListener;
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
	private MainCommand command;
	private UUID proxyID;
	
	
	@Override
	public void onEnable() {
		this.setupProxyId();
		this.adventure = BungeeAudiences.create(this);
		this.taskManager = new BungeeTaskManager(this);
		this.database = new MariaDB(this);
		this.transactionLogger = new HikariDBLogger(this).init();
		this.userManager = new BungeeUserManager(this).init();
		this.command = new MainCommand(this, "dpb", "portfel.command", "devportfelbungee");
		this.getProxy().getPluginManager().registerCommand(this, this.command);
		this.getProxy().getPluginManager().registerListener(this, new UserListener(this));
		this.getProxy().getPluginManager().registerListener(this, new ChannelListener(this));
	}
	
	
	@Override
	public void onDisable() {
		this.userManager.killManager();
		this.transactionLogger.killLogger();
		this.database.shutdown();
		this.taskManager.cancelAll();
		try {
			Field f = Class.forName("net.kyori.adventure.platform.bungeecord.BungeeAudiencesImpl").getDeclaredField("INSTANCES");
			f.setAccessible(true);
			Map<?, ?> INSTANCES = (Map<?, ?>) f.get(null);
			INSTANCES.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getProxy().getPluginManager().unregisterListeners(this);
	}
	
	
	public UUID getProxyId() {
		return this.proxyID;
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
	
	public BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	
	private void setupProxyId() {
		final File f = new File(this.getDataFolder(), "server-id.dat");
		if (f.exists()) {
			try {
				this.proxyID = UUID.fromString(Files.readString(f.toPath()));
				return;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
				File to = new File(this.getDataFolder(), "server-id.dat.old");
				if (to.exists()) to.delete();
				f.renameTo(to);
			}
		}
		try {
			Files.writeString(f.toPath(), (this.proxyID = UUID.randomUUID()).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
