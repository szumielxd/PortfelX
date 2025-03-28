package me.szumielxd.portfel.bungee;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.szumielxd.portfel.api.CommonLogger;
import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.ComponentMapper;
import me.szumielxd.portfel.bungee.commands.BungeeCommandWrapper;
import me.szumielxd.portfel.bungee.listeners.BungeeChannelListener;
import me.szumielxd.portfel.bungee.listeners.BungeeUserListener;
import me.szumielxd.portfel.bungee.managers.BungeeAccessManagerImpl;
import me.szumielxd.portfel.bungee.objects.BungeeComponentMapper;
import me.szumielxd.portfel.bungee.objects.BungeeProxy;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.luckperms.ContextProvider;
import me.szumielxd.portfel.common.managers.PrizesManager;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.commands.CommonCommand;
import me.szumielxd.portfel.proxy.commands.MainCommand;
import me.szumielxd.portfel.proxy.commands.MainTokenCommand;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.ProxyTaskManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyTopManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyUserManagerImpl;
import me.szumielxd.portfel.proxy.managers.TokenManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class PortfelBungeeImpl extends Plugin implements PortfelProxyImpl<BaseComponent[]> {
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	private AccessManagerImpl<PortfelBungeeImpl, BaseComponent[]> accessManager;
	private TaskManager taskManager;
	private @Getter ConfigImpl configuration;
	private ProxyUserManagerImpl<BaseComponent[]> userManager;
	private ProxyTopManagerImpl<BaseComponent[]> topManager;
	private OrdersManager ordersManager;
	private PrizesManager<BaseComponent[]> prizesManager;
	private TokenManager<BaseComponent[]> tokenManager;
	private @Getter @Setter AbstractDB database;
	private @Getter @Setter AbstractTokenDB tokenDatabase;
	private @Getter @Setter AbstractDBLogger transactionLogger;
	private @Getter MainCommand<BaseComponent[]> command;
	private @Getter MainTokenCommand<BaseComponent[]> tokenCommand;
	private @Getter @Setter UUID proxyId;
	private ContextProvider<ProxiedPlayer, BaseComponent[]> luckpermsContextProvider;
	private BungeeComponentMapper componentMapper = new BungeeComponentMapper();
	private @Getter @Nullable BungeeProxy commonServer = new BungeeProxy(this);
	private final @Accessors(fluent = true) @Getter @NotNull CommonLogger logger = new BungeeLogger(getLogger());
	
	
	@Override
	public void onEnable() {
		PortfelProvider.register(this);
		CommonArgs.init(this);
		this.setupProxyId();
		this.load();
		this.taskManager = new ProxyTaskManagerImpl(this);
		this.accessManager = new BungeeAccessManagerImpl(this).init();
		//
		this.setupDatabases();
		
		this.getLogger().info("Setup managers...");
		this.userManager = new ProxyUserManagerImpl<>(this).init();
		this.topManager = new ProxyTopManagerImpl<>(this).init();
		this.tokenManager = new TokenManager<>(this).init();
		this.getLogger().info("Registering listeners...");
		this.getProxy().getPluginManager().registerListener(this, new BungeeUserListener(this));
		this.getProxy().getPluginManager().registerListener(this, new BungeeChannelListener(this));
		this.getLogger().info("Registering commands...");
		this.command = new MainCommand<>(this, "dpb", "portfel.command", "devportfelbungee");
		this.tokenCommand = new MainTokenCommand<>(this, this.configuration.getString(ProxyConfigKey.TOKEN_COMMAND_NAME), this.configuration.getStringList(ProxyConfigKey.TOKEN_COMMAND_ALIASES).toArray(String[]::new));
		this.registerCommand(this.command);
		this.registerCommand(this.tokenCommand);
		this.getProxy().registerChannel(CHANNEL_SETUP);
		this.getProxy().registerChannel(CHANNEL_INFO);
		this.getProxy().registerChannel(CHANNEL_TRANSACTIONS);
		
		this.sendMotd();
		
	}
	
	
	private void registerCommand(@NotNull CommonCommand<BaseComponent[]> command) {
		this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandWrapper(this, command));
	}
	
	
	public void load() {
		this.getLogger().info("Loading configuration...");
		this.configuration = new ConfigImpl(this).init(Stream.concat(Stream.of(ConfigKey.values()), Stream.of(ProxyConfigKey.values())).toArray(AbstractKey[]::new));
		this.getLogger().info("Setup locales...");
		LangKey.registerAll(MainLangKey.class, ProxyLangKey.class);
		Lang.load(this.getDataDirectory().resolve("languages"), this);
		this.ordersManager = new OrdersManager(this).init();
		this.prizesManager = new PrizesManager<>(this).init();
		if (this.getProxy().getPluginManager().getPlugin("LuckPerms") != null) {
			this.luckpermsContextProvider = new ContextProvider<>(this, ProxiedPlayer.class);
		}
	}
	
	
	public void unload() {
		LangKey.killThemAll();
		this.getLogger().info("Unregistering external hooks");
		if (this.luckpermsContextProvider != null) {
			this.luckpermsContextProvider.unregisterAll();
		}
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
			instances.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getLogger().info("Unregistering commands");
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getLogger().info("Unregistering listeners");
		this.getProxy().getPluginManager().unregisterListeners(this);
		this.getLogger().info("Unregistering channels");
		this.getProxy().unregisterChannel(CHANNEL_SETUP);
		this.getProxy().unregisterChannel(CHANNEL_INFO);
		this.getProxy().unregisterChannel(CHANNEL_TRANSACTIONS);
		this.unload();
		this.getLogger().info("Everything OK, miss you");
		this.getLogger().info("Goodbye my friend...");
	}
	
	
	public @NotNull AccessManagerImpl<PortfelBungeeImpl, BaseComponent[]> getAccessManager() {
		return this.accessManager;
	}
	
	
	public @NotNull TokenManager<BaseComponent[]> getTokenManager() {
		return this.tokenManager;
	}


	@Override
	public @NotNull ProxyUserManagerImpl<BaseComponent[]> getUserManager() {
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
	
	public @NotNull PrizesManager<BaseComponent[]> getPrizesManager() {
		return this.prizesManager;
	}
	
	/**
	 * Get Console Sender.
	 * 
	 * @return current console sender
	 */
	public @NotNull CommonSender<BaseComponent[]> getConsole() {
		return this.getCommonServer().getConsole();
	}
	
	
	private void sendMotd() {
		this.getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv"+this.getDescription().getVersion()+"\u001b[0m");
		this.getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on BungeeCord - " + this.getProxy().getName() + "\u001b[0m");
		this.getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}




	@Override
	public @NotNull Path getDataDirectory() {
		return this.getDataFolder().toPath();
	}


	@Override
	public @NotNull ComponentMapper<BaseComponent[]> getComponentMapper() {
		return this.componentMapper;
	}
	

}
