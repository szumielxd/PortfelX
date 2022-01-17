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
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bungee.commands.BungeeCommandWrapper;
import me.szumielxd.portfel.bungee.listeners.BungeeChannelListener;
import me.szumielxd.portfel.bungee.listeners.BungeeUserListener;
import me.szumielxd.portfel.bungee.managers.BungeeAccessManagerImpl;
import me.szumielxd.portfel.bungee.objects.BungeeProxy;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.ValidateAccess;
import me.szumielxd.portfel.common.luckperms.ContextProvider;
import me.szumielxd.portfel.common.managers.PrizesManager;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;
import me.szumielxd.portfel.proxy.api.objects.CommonProxy;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.commands.CommonCommand;
import me.szumielxd.portfel.proxy.commands.MainCommand;
import me.szumielxd.portfel.proxy.commands.MainTokenCommand;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger;
import me.szumielxd.portfel.proxy.database.hikari.MariaDB;
import me.szumielxd.portfel.proxy.database.hikari.MysqlDB;
import me.szumielxd.portfel.proxy.database.hikari.logging.HikariDBLogger;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.database.token.hikari.MariaTokenDB;
import me.szumielxd.portfel.proxy.database.token.hikari.MysqlTokenDB;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.ProxyTaskManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyTopManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyUserManagerImpl;
import me.szumielxd.portfel.proxy.managers.TokenManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;

public class PortfelBungeeImpl extends Plugin implements PortfelProxyImpl {
	
	
	
	private @NotNull BungeeProxy proxy;
	
	
	@Override
	public @NotNull CommonProxy getProxyServer() {
		return this.proxy;
	}
	
	
	private void registerCommand(@NotNull CommonCommand command) {
		this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandWrapper(this, command));
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	private BungeeAudiences adventure;
	private AccessManagerImpl accessManager;
	private TaskManager taskManager;
	private ConfigImpl config;
	private ProxyUserManagerImpl userManager;
	private ProxyTopManagerImpl topManager;
	private OrdersManager ordersManager;
	private PrizesManager prizesManager;
	private TokenManager tokenManager;
	private AbstractDB database;
	private AbstractTokenDB tokenDatabase;
	private AbstractDBLogger transactionLogger;
	private MainCommand command;
	private MainTokenCommand tokenCommand;
	private UUID proxyID;
	
	private ContextProvider luckpermsContextProvider;
	
	
	@Override
	public void onEnable() {
		if (ValidateAccess.checkAccess() == false) {
			this.getLogger().warning("You have no power here. Die potato!");
			return;
		}
		PortfelProvider.register(this);
		CommonArgs.init(this);
		this.setupProxyId();
		this.proxy = new BungeeProxy(this);
		this.adventure = BungeeAudiences.create(this);
		this.taskManager = new ProxyTaskManagerImpl(this);
		this.accessManager = new BungeeAccessManagerImpl(this).init();
		this.load();
		//
		String dbType = this.getConfiguration().getString(ProxyConfigKey.DATABASE_TYPE).toLowerCase();
		if ("mariadb".equals(dbType)) this.database = new MariaDB(this);
		else this.database = new MysqlDB(this);
		this.getLogger().info("Establishing connection with database...");
		this.database.setup();
		//
		String tokenDbType = this.getConfiguration().getString(ProxyConfigKey.TOKEN_DATABASE_TYPE).toLowerCase();
		if ("mariadb".equals(tokenDbType)) this.tokenDatabase = new MariaTokenDB(this);
		else this.tokenDatabase = new MysqlTokenDB(this);
		this.getLogger().info("Establishing connection with tokens database...");
		this.tokenDatabase.setup();
		
		this.getLogger().info("Setup managers...");
		this.transactionLogger = new HikariDBLogger(this).init();
		this.userManager = new ProxyUserManagerImpl(this).init();
		this.topManager = (ProxyTopManagerImpl) new ProxyTopManagerImpl(this).init();
		this.tokenManager = new TokenManager(this).init();
		this.getLogger().info("Registering listeners...");
		this.getProxy().getPluginManager().registerListener(this, new BungeeUserListener(this));
		this.getProxy().getPluginManager().registerListener(this, new BungeeChannelListener(this));
		this.getLogger().info("Registering commands...");
		this.command = new MainCommand(this, "dpb", "portfel.command", "devportfelbungee");
		this.tokenCommand = new MainTokenCommand(this, this.config.getString(ProxyConfigKey.TOKEN_COMMAND_NAME), this.config.getStringList(ProxyConfigKey.TOKEN_COMMAND_ALIASES).toArray(new String[0]));
		this.registerCommand(this.command);
		this.registerCommand(this.tokenCommand);
		this.getProxy().registerChannel(CHANNEL_SETUP);
		this.getProxy().registerChannel(CHANNEL_USERS);
		this.getProxy().registerChannel(CHANNEL_TRANSACTIONS);
		
		this.sendMotd();
		
	}
	
	
	public void load() {
		this.getLogger().info("Loading configuration...");
		this.config = new ConfigImpl(this).init(MiscUtils.mergeArrays(Stream.of(ConfigKey.values()).toArray(AbstractKey[]::new), Stream.of(ProxyConfigKey.values()).toArray(AbstractKey[]::new)));
		this.getLogger().info("Setup locales...");
		Lang.load(new File(this.getDataFolder(), "languages"), this);
		this.ordersManager = new OrdersManager(this).init();
		this.prizesManager = new PrizesManager(this).init();
		if (this.getProxy().getPluginManager().getPlugin("LuckPerms") != null) {
			this.luckpermsContextProvider = new ContextProvider(this);
		}
	}
	
	
	public void unload() {
		this.getLogger().info("Unregistering external hooks");
		if(this.luckpermsContextProvider != null) this.luckpermsContextProvider.unregisterAll();
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Unloading managers");
		this.userManager.killManager();
		this.topManager.killManager();
		this.tokenManager.killManager();
		this.transactionLogger.killLogger();
		this.database.shutdown();
		this.tokenDatabase.shutdown();
		this.taskManager.cancelAll();
		this.getLogger().info("Unhooking kyori adventure");
		try {
			Field f = Class.forName("net.kyori.adventure.platform.bungeecord.BungeeAudiencesImpl").getDeclaredField("INSTANCES");
			f.setAccessible(true);
			Map<?, ?> INSTANCES = (Map<?, ?>) f.get(null);
			INSTANCES.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getLogger().info("Unregistering commands");
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getLogger().info("Unregistering listeners");
		this.getProxy().getPluginManager().unregisterListeners(this);
		this.getLogger().info("Unregistering channels");
		this.getProxy().unregisterChannel(CHANNEL_SETUP);
		this.getProxy().unregisterChannel(CHANNEL_USERS);
		this.getProxy().unregisterChannel(CHANNEL_TRANSACTIONS);
		this.unload();
		this.getLogger().info("Everything OK, miss you");
		this.getLogger().info("Goodbye my friend...");
	}
	
	
	public @NotNull UUID getProxyId() {
		return this.proxyID;
	}
	
	
	public @NotNull AbstractDB getDB() {
		return this.database;
	}
	
	
	public @NotNull AbstractTokenDB getTokenDB() {
		return this.tokenDatabase;
	}
	
	
	public @NotNull AccessManagerImpl getAccessManager() {
		return this.accessManager;
	}
	
	
	public @NotNull TokenManager getTokenManager() {
		return this.tokenManager;
	}


	@Override
	public @NotNull UserManager getUserManager() {
		return this.userManager;
	}
	
	
	@Override
	public @NotNull ProxyTopManager getTopManager() {
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
	
	
	@Override
	public @NotNull String getVersion() {
		return this.getDescription().getVersion();
	}
	
	
	@Override
	public @NotNull String getAuthor() {
		return this.getDescription().getAuthor();
	}
	
	
	@Override
	public @NotNull String getDescriptionText() {
		return this.getDescription().getDescription();
	}
	
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
	public @NotNull PrizesManager getPrizesManager() {
		return this.prizesManager;
	}
	
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	public @NotNull AbstractDBLogger getDBLogger() {
		return this.transactionLogger;
	}
	
	/**
	 * Get Bungee audience implementation.
	 * 
	 * @return audiences
	 */
	@Override
	public @NotNull BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	/**
	 * Get Console Sender.
	 * 
	 * @return current console sender
	 */
	public @NotNull CommonSender getConsole() {
		return this.getProxyServer().getConsole();
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
		this.getProxy().getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getProxy().getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv"+this.getDescription().getVersion()+"\u001b[0m");
		this.getProxy().getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on BungeeCord - " + this.getProxy().getName() + "\u001b[0m");
		this.getProxy().getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getProxy().getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}


	@Override
	public @NotNull Config getConfiguration() {
		return this.config;
	}
	

}
