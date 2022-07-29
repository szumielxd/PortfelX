package me.szumielxd.portfel.bungee;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;
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
import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.CommonLogger;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.luckperms.ContextProvider;
import me.szumielxd.portfel.common.managers.PrizesManager;
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
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.ProxyTaskManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyTopManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyUserManagerImpl;
import me.szumielxd.portfel.proxy.managers.TokenManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class PortfelBungeeImpl implements PortfelProxyImpl, LoadablePortfel {
	
	
	private final @NotNull PortfelBungeeBootstrap bootstrap;
	
	
	private @Nullable BungeeProxy proxy;
	
	
	public PortfelBungeeImpl(@NotNull PortfelBungeeBootstrap bootstrap) {
		this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap cannot be null");
	}
	

	public Plugin asPlugin() {
		return this.bootstrap;
	}


	@Override
	public void addToRuntime(CommonDependency... dependency) {
		this.bootstrap.addToRuntime(dependency);
	}
	
	
	@Override
	public @NotNull CommonProxy getCommonServer() {
		return this.proxy;
	}
	
	
	private void registerCommand(@NotNull CommonCommand command) {
		this.asPlugin().getProxy().getPluginManager().registerCommand(this.asPlugin(), new BungeeCommandWrapper(this, command));
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	private BungeeAudiences adventure;
	private AccessManagerImpl accessManager;
	private TaskManager taskManager;
	private @Getter ConfigImpl configuration;
	private ProxyUserManagerImpl userManager;
	private ProxyTopManagerImpl topManager;
	private OrdersManager ordersManager;
	private PrizesManager prizesManager;
	private TokenManager tokenManager;
	private @Getter @Setter AbstractDB database;
	private @Getter @Setter AbstractTokenDB tokenDatabase;
	private @Getter @Setter AbstractDBLogger transactionLogger;
	private @Getter MainCommand command;
	private @Getter MainTokenCommand tokenCommand;
	private @Getter @Setter UUID proxyId;
	
	private ContextProvider<ProxiedPlayer> luckpermsContextProvider;
	
	
	@Override
	public void onEnable() {
		if (!ValidateAccess.checkAccess()) {
			this.getLogger().warn("You have no power here. Die potato!");
			return;
		}
		PortfelProvider.register(this);
		CommonArgs.init(this);
		this.setupProxyId();
		this.load();
		this.proxy = new BungeeProxy(this);
		this.adventure = BungeeAudiences.create(this.asPlugin());
		this.taskManager = new ProxyTaskManagerImpl(this);
		this.accessManager = new BungeeAccessManagerImpl(this).init();
		//
		this.setupDatabases();
		
		this.getLogger().info("Setup managers...");
		this.userManager = new ProxyUserManagerImpl(this).init();
		this.topManager = new ProxyTopManagerImpl(this).init();
		this.tokenManager = new TokenManager(this).init();
		this.getLogger().info("Registering listeners...");
		this.asPlugin().getProxy().getPluginManager().registerListener(this.asPlugin(), new BungeeUserListener(this));
		this.asPlugin().getProxy().getPluginManager().registerListener(this.asPlugin(), new BungeeChannelListener(this));
		this.getLogger().info("Registering commands...");
		this.command = new MainCommand(this, "dpb", "portfel.command", "devportfelbungee");
		this.tokenCommand = new MainTokenCommand(this, this.configuration.getString(ProxyConfigKey.TOKEN_COMMAND_NAME), this.configuration.getStringList(ProxyConfigKey.TOKEN_COMMAND_ALIASES).toArray(new String[0]));
		this.registerCommand(this.command);
		this.registerCommand(this.tokenCommand);
		this.asPlugin().getProxy().registerChannel(CHANNEL_SETUP);
		this.asPlugin().getProxy().registerChannel(CHANNEL_USERS);
		this.asPlugin().getProxy().registerChannel(CHANNEL_TRANSACTIONS);
		
		this.sendMotd();
		
	}
	
	
	public void load() {
		this.getLogger().info("Loading configuration...");
		this.configuration = new ConfigImpl(this).init(Stream.of(ConfigKey.values(), ProxyConfigKey.values()).flatMap(Stream::of).toArray(AbstractKey[]::new));
		this.getLogger().info("Setup locales...");
		Lang.load(this.getDataFolder().resolve("languages"), this);
		this.ordersManager = new OrdersManager(this).init();
		this.prizesManager = new PrizesManager(this).init();
		if (this.asPlugin().getProxy().getPluginManager().getPlugin("LuckPerms") != null) {
			this.luckpermsContextProvider = new ContextProvider<>(this, ProxiedPlayer.class);
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
			Map<?, ?> instances = (Map<?, ?>) f.get(null);
			instances.remove(this.asPlugin().getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getLogger().info("Unregistering commands");
		this.asPlugin().getProxy().getPluginManager().unregisterCommands(this.asPlugin());
		this.getLogger().info("Unregistering listeners");
		this.asPlugin().getProxy().getPluginManager().unregisterListeners(this.asPlugin());
		this.getLogger().info("Unregistering channels");
		this.asPlugin().getProxy().unregisterChannel(CHANNEL_SETUP);
		this.asPlugin().getProxy().unregisterChannel(CHANNEL_USERS);
		this.asPlugin().getProxy().unregisterChannel(CHANNEL_TRANSACTIONS);
		this.unload();
		this.getLogger().info("Everything OK, miss you");
		this.getLogger().info("Goodbye my friend...");
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
		return this.asPlugin().getDescription().getName();
	}
	
	
	@Override
	public @NotNull String getVersion() {
		return this.asPlugin().getDescription().getVersion();
	}
	
	
	@Override
	public @NotNull String getAuthor() {
		return this.asPlugin().getDescription().getAuthor();
	}
	
	
	@Override
	public @NotNull String getDescriptionText() {
		return this.asPlugin().getDescription().getDescription();
	}
	
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
	public @NotNull PrizesManager getPrizesManager() {
		return this.prizesManager;
	}
	
	/**
	 * Get Bungee audience implementation.
	 * 
	 * @return audiences
	 */
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
		return this.getCommonServer().getConsole();
	}
	
	
	private void sendMotd() {
		this.getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv"+this.asPlugin().getDescription().getVersion()+"\u001b[0m");
		this.getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on BungeeCord - " + this.asPlugin().getProxy().getName() + "\u001b[0m");
		this.getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}




	@Override
	public @NotNull Path getDataFolder() {
		return this.asPlugin().getDataFolder().toPath();
	}




	@Override
	public @NotNull CommonLogger getLogger() {
		return this.bootstrap.getCommonLogger();
	}
	

}
