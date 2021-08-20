package me.szumielxd.portfel.bungee;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Config;
import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.bungee.api.PortfelBungee;
import me.szumielxd.portfel.bungee.api.configuration.BungeeConfigKey;
import me.szumielxd.portfel.bungee.api.managers.BungeeTopManager;
import me.szumielxd.portfel.bungee.commands.CommonArgs;
import me.szumielxd.portfel.bungee.commands.MainCommand;
import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.database.AbstractDBLogger;
import me.szumielxd.portfel.bungee.database.hikari.MariaDB;
import me.szumielxd.portfel.bungee.database.hikari.MysqlDB;
import me.szumielxd.portfel.bungee.database.hikari.logging.HikariDBLogger;
import me.szumielxd.portfel.bungee.listeners.ChannelListener;
import me.szumielxd.portfel.bungee.listeners.UserListener;
import me.szumielxd.portfel.bungee.managers.AccessManagerImpl;
import me.szumielxd.portfel.bungee.managers.BungeeTaskManagerImpl;
import me.szumielxd.portfel.bungee.managers.BungeeTopManagerImpl;
import me.szumielxd.portfel.bungee.managers.BungeeUserManagerImpl;
import me.szumielxd.portfel.bungee.managers.OrdersManager;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.ValidateAccess;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;

public class PortfelBungeeImpl extends Plugin implements PortfelBungee {
	
	
	private BungeeAudiences adventure;
	private AccessManagerImpl accessManager;
	private TaskManager taskManager;
	private ConfigImpl config;
	private BungeeUserManagerImpl userManager;
	private BungeeTopManagerImpl topManager;
	private OrdersManager ordersManager;
	private AbstractDB database;
	private AbstractDBLogger transactionLogger;
	private MainCommand command;
	private UUID proxyID;
	
	
	@Override
	public void onEnable() {
		if (ValidateAccess.checkAccess() == false) {
			this.getLogger().warning("You have no power here. Die potato!");
			return;
		}
		PortfelProvider.register(this);
		CommonArgs.init(this);
		this.setupProxyId();
		this.adventure = BungeeAudiences.create(this);
		this.taskManager = new BungeeTaskManagerImpl(this);
		this.accessManager = new AccessManagerImpl(this).init();
		this.getLogger().info("Loading configuration...");
		this.config = new ConfigImpl(this).init(MiscUtils.mergeArrays(Stream.of(ConfigKey.values()).toArray(AbstractKey[]::new), Stream.of(BungeeConfigKey.values()).toArray(AbstractKey[]::new)));
		this.getLogger().info("Setup locales...");
		Lang.load(new File(this.getDataFolder(), "languages"), this);
		String dbType = this.getConfiguration().getString(BungeeConfigKey.DATABASE_TYPE).toLowerCase();
		if ("mariadb".equals(dbType)) this.database = new MariaDB(this);
		else this.database = new MysqlDB(this);
		this.getLogger().info("Establishing connection with database...");
		this.database.setup();
		
		this.getLogger().info("Setup managers...");
		this.transactionLogger = new HikariDBLogger(this).init();
		this.userManager = new BungeeUserManagerImpl(this).init();
		this.topManager = (BungeeTopManagerImpl) new BungeeTopManagerImpl(this).init();
		this.ordersManager = new OrdersManager(this).init();
		this.getLogger().info("Registering listeners...");
		this.getProxy().getPluginManager().registerListener(this, new UserListener(this));
		this.getProxy().getPluginManager().registerListener(this, new ChannelListener(this));
		this.getLogger().info("Registering commands...");
		this.command = new MainCommand(this, "dpb", "portfel.command", "devportfelbungee");
		this.getProxy().getPluginManager().registerCommand(this, this.command);
		this.getProxy().registerChannel(CHANNEL_SETUP);
		this.getProxy().registerChannel(CHANNEL_USERS);
		this.getProxy().registerChannel(CHANNEL_TRANSACTIONS);
		this.getProxy().registerChannel(CHANNEL_BUNGEE);
		this.sendMotd();
		
	}
	
	
	@Override
	public void onDisable() {
		this.userManager.killManager();
		this.topManager.killManager();
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
		this.getProxy().unregisterChannel(CHANNEL_SETUP);
		this.getProxy().unregisterChannel(CHANNEL_USERS);
		this.getProxy().unregisterChannel(CHANNEL_TRANSACTIONS);
	}
	
	
	public @NotNull UUID getProxyId() {
		return this.proxyID;
	}
	
	
	public @NotNull AbstractDB getDB() {
		return this.database;
	}
	
	
	public @NotNull AccessManagerImpl getAccessManager() {
		return this.accessManager;
	}


	@Override
	public @NotNull UserManager getUserManager() {
		return this.userManager;
	}
	
	
	@Override
	public @NotNull BungeeTopManager getTopManager() {
		return this.topManager;
	}
	
	
	@Override
	public @NotNull TaskManager getTaskManager() {
		return this.taskManager;
	}
	
	@Override
	public @NotNull String getName() {
		return this.getDescription().getName();
	}
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	public @NotNull AbstractDBLogger getDBLogger() {
		return this.transactionLogger;
	}
	
	public @NotNull BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	
	private void setupProxyId() {
		final File f = new File(this.getDataFolder(), "server-id.dat");
		if (f.exists()) {
			try {
				this.proxyID = UUID.fromString(String.join("\n", Files.readAllLines(f.toPath())));
				return;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
				File to = new File(this.getDataFolder(), "server-id.dat.broken");
				if (to.exists()) to.delete();
				f.renameTo(to);
			}
		}
		try {
			File parent = f.getParentFile();
			if (!parent.exists()) parent.mkdirs();
			Files.write(f.toPath(), (this.proxyID = UUID.randomUUID()).toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void sendMotd() {
		this.getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐");
		this.getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv3.0.0");
		this.getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on BungeeCord - " + this.getProxy().getName());
		this.getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘");
		this.getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘");
	}


	@Override
	public @NotNull Config getConfiguration() {
		return this.config;
	}
	

}
