package me.szumielxd.portfel.bukkit;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.github.curiousoddman.rgxgen.RgxGen;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.Config;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.CommonServer;
import me.szumielxd.portfel.bukkit.api.PortfelBukkit;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.api.managers.BukkitTopManager;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.api.managers.IdentifierManager;
import me.szumielxd.portfel.bukkit.commands.MainCommand;
import me.szumielxd.portfel.bukkit.commands.WalletCommand;
import me.szumielxd.portfel.bukkit.hooks.MVdWHandler;
import me.szumielxd.portfel.bukkit.hooks.PAPIHandler;
import me.szumielxd.portfel.bukkit.listeners.GuiListener;
import me.szumielxd.portfel.bukkit.listeners.UserListener;
import me.szumielxd.portfel.bukkit.managers.BukkitTaskManagerImpl;
import me.szumielxd.portfel.bukkit.managers.BukkitTopManagerImpl;
import me.szumielxd.portfel.bukkit.managers.BukkitUserManagerImpl;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl;
import me.szumielxd.portfel.bukkit.managers.IdentifierManagerImpl;
import me.szumielxd.portfel.bukkit.managers.OrdersManager;
import me.szumielxd.portfel.bukkit.objects.BukkitServer;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.ValidateAccess;
import me.szumielxd.portfel.common.loader.CommonLogger;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.luckperms.ContextProvider;
import me.szumielxd.portfel.common.managers.PrizesManager;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class PortfelBukkitImpl implements PortfelBukkit, LoadablePortfel {
	
	
	private final @NotNull PortfelBukkitBootstrap bootstrap;
	private @NotNull CommonServer server;
	
	
	private BukkitAudiences adventure;
	private Config config;
	private TaskManager taskManager;
	private IdentifierManager identifierManager;
	private ChannelManagerImpl channelManager;
	private OrdersManager ordersManager;
	private PrizesManager prizesManager;
	private BukkitUserManagerImpl userManager;
	private BukkitTopManager topManager;
	
	private PAPIHandler papiHandler;
	private MVdWHandler mvdwHandler;
	private ContextProvider<Player> luckpermsContextProvider;
	
	private String serverHashKey;
	
	
	public PortfelBukkitImpl(@NotNull PortfelBukkitBootstrap bootstrap) {
		this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap cannot be null");
		this.server = new BukkitServer(this);
	}
	
	
	public JavaPlugin asPlugin() {
		return this.bootstrap;
	}


	@Override
	public @NotNull CommonServer getCommonServer() {
		return this.server;
	}
	
	
	@Override
	public void onEnable() {
		if (!ValidateAccess.checkAccess()) {
			this.getLogger().warn("You have no power here. Die potato!");
			this.getServer().getPluginManager().disablePlugin(this.asPlugin());
			return;
		}
		PortfelProvider.register(this);
		try {
			this.adventure = BukkitAudiences.create(this.asPlugin());
		} catch (NoSuchFieldError e) {
			// older kyori version, let my try to hook into paper native kyori support
		}
		this.taskManager = new BukkitTaskManagerImpl(this);
		
		this.load();
		
		if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			this.mvdwHandler = new MVdWHandler(this);
		}
		
		this.getLogger().info("Setup managers...");
		this.channelManager = new ChannelManagerImpl(this);
		this.userManager = new BukkitUserManagerImpl(this).init();
		this.topManager = new BukkitTopManagerImpl(this).init();
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new GuiListener(), this.asPlugin());
		this.getServer().getPluginManager().registerEvents(new UserListener(this), this.asPlugin());
		this.getLogger().info("Registering commands...");
		try {
			SimpleCommandMap commands = this.getCommandMap();
			// main
			PluginCommand mainCmd = this.getPluginCommand("devportfel");
			mainCmd.setAliases(Arrays.asList("dp"));
			mainCmd.setDescription("Portfel main command");
			mainCmd.setExecutor(new MainCommand(this, mainCmd));
			mainCmd.setPermission("portfel.command");
			mainCmd.setUsage("/<command>");
			commands.register(this.getName(), mainCmd);
			// wallet
			PluginCommand walletCmd = this.getPluginCommand(this.getConfiguration().getString(BukkitConfigKey.SHOP_COMMAND_NAME));
			walletCmd.setAliases(this.getConfiguration().getStringList(BukkitConfigKey.SHOP_COMMAND_ALIASES));
			walletCmd.setDescription("Command to open wallet shop gui");
			walletCmd.setExecutor(new WalletCommand(this));
			walletCmd.setPermission("portfel.gui");
			walletCmd.setUsage("/<command>");
			commands.register(this.getName(), walletCmd);
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.sendMotd();
	}
	
	
	public void load() {
		this.identifierManager = new IdentifierManagerImpl(this).init();
		this.getLogger().info("Loading configuration...");
		this.config = new ConfigImpl(this).init(MiscUtils.mergeArrays(Stream.of(ConfigKey.values()).toArray(AbstractKey[]::new), Stream.of(BukkitConfigKey.values()).toArray(AbstractKey[]::new)));
		this.getLogger().info("Setup locales...");
		Lang.load(this.getDataFolder().resolve("languages"), this);
		this.setupBukkitKey();
		this.ordersManager = new OrdersManager(this).init();
		this.prizesManager = new PrizesManager(this).init();
		if(this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			this.papiHandler = new PAPIHandler(this);
		}
		if(this.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
			this.luckpermsContextProvider = new ContextProvider<>(this, Player.class);
		}
	}
	
	
	public void unload() {
		this.getLogger().info("Unregistering external hooks");
		if(this.papiHandler != null) this.papiHandler.oldUnregister();
		if(this.luckpermsContextProvider != null) this.luckpermsContextProvider.unregisterAll();
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Unloading managers");
		if (this.userManager != null) this.userManager.killManager();
		if (this.userManager != null) this.topManager.killManager();
		if (this.userManager != null) this.taskManager.cancelAll();
		this.getLogger().info("Unregistering channels");
		if (this.userManager != null) this.channelManager.killManager();
		this.getLogger().info("Unhooking kyori adventure");
		try {
			Field f = Class.forName("net.kyori.adventure.platform.bukkit.BukkitAudiencesImpl").getDeclaredField("INSTANCES");
			f.setAccessible(true);
			Map<?, ?> instances = (Map<?, ?>) f.get(null);
			instances.remove(this.asPlugin().getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// no kyori today :c
		}
		this.getLogger().info("Unregistering commands");
		try {
			SimpleCommandMap commands = this.getCommandMap();
			commands.getCommands().stream().filter(PluginCommand.class::isInstance).map(PluginCommand.class::cast)
					.filter(cmd -> this.asPlugin().equals(cmd.getPlugin())).forEach(cmd -> cmd.unregister(commands));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getLogger().info("Unregistering listeners");
		HandlerList.unregisterAll(this.asPlugin());
		
		this.unload();
		if (this.mvdwHandler != null) this.mvdwHandler.unregister();
		this.getLogger().info("Everything OK, miss you");
		this.getLogger().info("Goodbye my friend...");
	}

	/**
	 * Get user manager.
	 * 
	 * @return user manager
	 */
	@Override
	public @NotNull UserManager getUserManager() {
		return this.userManager;
	}
	
	/**
	 * Get top manager.
	 * 
	 * @return top manager
	 */
	@Override
	public @NotNull BukkitTopManager getTopManager() {
		return this.topManager;
	}

	/**
	 * Get task manager.
	 * 
	 * @return task manager
	 */
	@Override
	public @NotNull TaskManager getTaskManager() {
		return this.taskManager;
	}

	/**
	 * Get plugin's configuration.
	 * 
	 * @return plugin's configuration
	 */
	@Override
	public @NotNull Config getConfiguration() {
		return this.config;
	}
	
	/**
	 * Get channel manager.
	 * 
	 * @return channel manager
	 */
	@Override
	public @NotNull ChannelManager getChannelManager() {
		return this.channelManager;
	}
	
	/**
	 * Get identifier manager.
	 * 
	 * @return identifier manager
	 */
	public @NotNull IdentifierManager getIdentifierManager() {
		return this.identifierManager;
	}
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
	public @NotNull PrizesManager getPrizesManager() {
		return this.prizesManager;
	}
	
	/**
	 * Get Bukkit audience implementation.
	 * 
	 * @return audiences
	 */
	@Override
	public @NotNull BukkitAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	/**
	 * Get Console Sender.
	 * 
	 * @return current console sender
	 */
	@Override
	public @NotNull CommonSender getConsole() {
		return this.getCommonServer().getConsole();
	}
	
	
	public @NotNull String getServerHashKey() {
		return this.serverHashKey;
	}
	
	
	private SimpleCommandMap getCommandMap() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = this.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
		f.setAccessible(true);
		return (SimpleCommandMap) f.get(this.getServer().getPluginManager());
	}
	
	
	private @NotNull PluginCommand getPluginCommand(String name) {
		try {
			Constructor<PluginCommand> constr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constr.setAccessible(true);
			return constr.newInstance(name, this.asPlugin());
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void sendMotd() {
		this.getServer().getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv"+this.asPlugin().getDescription().getVersion()+"\u001b[0m");
		this.getServer().getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on Bukkit - " + this.getServer().getName() + "\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}
	
	
	private void setupBukkitKey() {
		final Path f = this.getDataFolder().resolve("server-key.dat");
		if (Files.exists(f)) {
			try {
				this.serverHashKey = String.join("\n", Files.readAllLines(f));
				return;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
				Path to = this.getDataFolder().resolve(f.getFileName() + ".broken");
				try {
					Files.move(f, to, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
		try {
			if (!Files.exists(f.getParent())) Files.createDirectories(f.getParent());
			this.serverHashKey = new RgxGen("[a-zA-Z0-9]{16}").generate();
			Files.write(f, this.serverHashKey.getBytes(StandardCharsets.US_ASCII));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public @NotNull Path getDataFolder() {
		return this.bootstrap.getDataFolderPath();
	}


	@Override
	public @NotNull String getName() {
		return this.bootstrap.getName();
	}


	@Override
	public @NotNull CommonLogger getLogger() {
		return this.bootstrap.getCommonLogger();
	}


	@Override
	public @NotNull Server getServer() {
		return this.bootstrap.getServer();
	}
	

}
