package me.szumielxd.portfel.velocity;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import lombok.Getter;
import lombok.Setter;
import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.Config;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.ValidateAccess;
import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.CommonLogger;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
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
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.ProxyTaskManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyTopManagerImpl;
import me.szumielxd.portfel.proxy.managers.ProxyUserManagerImpl;
import me.szumielxd.portfel.proxy.managers.TokenManager;
import me.szumielxd.portfel.velocity.commands.VelocityCommandWrapper;
import me.szumielxd.portfel.velocity.listeners.VelocityChannelListener;
import me.szumielxd.portfel.velocity.listeners.VelocityUserListener;
import me.szumielxd.portfel.velocity.managers.VelocityAccessManagerImpl;
import me.szumielxd.portfel.velocity.objects.VelocityProxy;

public class PortfelVelocityImpl implements PortfelProxyImpl, LoadablePortfel {
	
	
	private final @NotNull PortfelVelocityBootstrap bootstrap;
	
	
	private @Nullable VelocityProxy proxy;
	
	
	public PortfelVelocityImpl(PortfelVelocityBootstrap bootstrap) {
		this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap cannot be null");
	}
	
	
	public Object asPlugin() {
		return this.bootstrap;
	}
	
	
	@Override
	public void addToRuntime(CommonDependency... dependency) {
		this.bootstrap.addToRuntime(dependency);
	}
	
	
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
	    this.onEnable();
	}
	
	
	@Override
	public @NotNull CommonLogger getLogger() {
		return this.bootstrap.getCommonLogger();
	}
	
	
	@Override
	public @NotNull Path getDataFolder() {
		return this.bootstrap.getDataFolderPath();
	}
	
	
	public @NotNull ProxyServer getProxy() {
		return this.bootstrap.getProxy();
	}


	@Override
	public @NotNull CommonProxy getCommonServer() {
		return this.proxy;
	}
	
	
	private void registerCommand(@NotNull CommonCommand command) {
		CommandManager mgr = this.getProxy().getCommandManager();
		CommandMeta meta = mgr.metaBuilder(command.getName()).aliases(command.getAliases()).build();
		mgr.register(meta, new VelocityCommandWrapper(this, command));
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	private AccessManagerImpl accessManager;
	private TaskManager taskManager;
	private ConfigImpl config;
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
	
	private ContextProvider<Player> luckpermsContextProvider;
	
	
	@Override
	public void onEnable() {
		if (!ValidateAccess.checkAccess()) {
			this.getLogger().warn("You have no power here. Die potato!");
			return;
		}
		PortfelProvider.register(this);
		this.proxy = new VelocityProxy(this);
		CommonArgs.init(this);
		this.setupProxyId();
		this.load();
		this.taskManager = new ProxyTaskManagerImpl(this);
		this.accessManager = new VelocityAccessManagerImpl(this).init();
		//
		this.setupDatabases();
		
		this.getLogger().info("Setup managers...");
		this.userManager = new ProxyUserManagerImpl(this).init();
		this.topManager = new ProxyTopManagerImpl(this).init();
		this.tokenManager = new TokenManager(this).init();
		this.getLogger().info("Registering listeners...");
		this.getProxy().getEventManager().register(this.asPlugin(), new VelocityUserListener(this));
		this.getProxy().getEventManager().register(this.asPlugin(), new VelocityChannelListener(this));
		this.getLogger().info("Registering commands...");
		this.command = new MainCommand(this, "dpv", "portfel.command", "devportfelvelocity");
		this.tokenCommand = new MainTokenCommand(this, this.config.getString(ProxyConfigKey.TOKEN_COMMAND_NAME), this.config.getStringList(ProxyConfigKey.TOKEN_COMMAND_ALIASES).toArray(new String[0]));
		this.registerCommand(this.command);
		this.registerCommand(this.tokenCommand);
		this.getProxy().getChannelRegistrar().register(MinecraftChannelIdentifier.from(CHANNEL_SETUP));
		this.getProxy().getChannelRegistrar().register(MinecraftChannelIdentifier.from(CHANNEL_USERS));
		this.getProxy().getChannelRegistrar().register(MinecraftChannelIdentifier.from(CHANNEL_TRANSACTIONS));
		this.getProxy().getChannelRegistrar().register(MinecraftChannelIdentifier.from(CHANNEL_BUNGEE));
		
		this.sendMotd();
		
	}
	
	
	public void load() {
		this.getLogger().info("Loading configuration...");
		this.config = new ConfigImpl(this).init(MiscUtils.mergeArrays(Stream.of(ConfigKey.values()).toArray(AbstractKey[]::new), Stream.of(ProxyConfigKey.values()).toArray(AbstractKey[]::new)));
		this.getLogger().info("Setup locales...");
		Lang.load(this.getDataFolder().resolve("languages"), this);
		this.ordersManager = new OrdersManager(this).init();
		this.prizesManager = new PrizesManager(this).init();
		if (this.getProxy().getPluginManager().getPlugin("LuckPerms").isPresent()) {
			this.luckpermsContextProvider = new ContextProvider<>(this, Player.class);
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
		this.getLogger().info("Unregistering listeners");
		this.getProxy().getEventManager().unregisterListeners(this.asPlugin());
		this.getLogger().info("Unregistering channels");
		this.getProxy().getChannelRegistrar().unregister(MinecraftChannelIdentifier.from(CHANNEL_SETUP));
		this.getProxy().getChannelRegistrar().unregister(MinecraftChannelIdentifier.from(CHANNEL_USERS));
		this.getProxy().getChannelRegistrar().unregister(MinecraftChannelIdentifier.from(CHANNEL_TRANSACTIONS));
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
		return this.bootstrap.getName();
	}
	
	
	@Override
	public @NotNull String getVersion() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this.asPlugin()).getDescription().getVersion().orElse("");
	}
	
	
	@Override
	public @NotNull String getAuthor() {
		return String.join(", ", this.getProxy().getPluginManager().ensurePluginContainer(this.asPlugin()).getDescription().getAuthors());
	}
	
	
	@Override
	public @NotNull String getDescriptionText() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this.asPlugin()).getDescription().getDescription().orElse("");
	}
	
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
	public @NotNull PrizesManager getPrizesManager() {
		return this.prizesManager;
	}
	
	
	private void sendMotd() {
		this.getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv"+this.getVersion()+"\u001b[0m");
		this.getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on Velocity" + "" + "\u001b[0m");
		this.getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}


	@Override
	public @NotNull Config getConfiguration() {
		return this.config;
	}


	@Override
	public @NotNull CommonSender getConsole() {
		return this.getCommonServer().getConsole();
	}
	

}
