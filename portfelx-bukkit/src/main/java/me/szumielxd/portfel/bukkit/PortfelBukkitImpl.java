package me.szumielxd.portfel.bukkit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Config;
import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.bukkit.api.PortfelBukkit;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.api.managers.BukkitTopManager;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.api.managers.IdentifierManager;
import me.szumielxd.portfel.bukkit.commands.WalletCommand;
import me.szumielxd.portfel.bukkit.hooks.PAPIHandler;
import me.szumielxd.portfel.bukkit.listeners.GuiListener;
import me.szumielxd.portfel.bukkit.listeners.UserListener;
import me.szumielxd.portfel.bukkit.managers.BukkitTaskManagerImpl;
import me.szumielxd.portfel.bukkit.managers.BukkitTopManagerImpl;
import me.szumielxd.portfel.bukkit.managers.BukkitUserManagerImpl;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl;
import me.szumielxd.portfel.bukkit.managers.IdentifierManagerImpl;
import me.szumielxd.portfel.bukkit.managers.OrdersManager;
import me.szumielxd.portfel.common.ConfigImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.ValidateAccess;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class PortfelBukkitImpl extends JavaPlugin implements PortfelBukkit {
	
	
	private BukkitAudiences adventure;
	private Config config;
	private TaskManager taskManager;
	private IdentifierManager identifierManager;
	private ChannelManagerImpl channelManager;
	private OrdersManager ordersManager;
	private BukkitUserManagerImpl userManager;
	private BukkitTopManager topManager;
	
	private PAPIHandler papiHandler;
	
	
	@Override
	public void onEnable() {
		if (ValidateAccess.checkAccess() == false) {
			this.getLogger().warning("You have no power here. Die potato!");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		PortfelProvider.register(this);
		this.adventure = BukkitAudiences.create(this);
		this.taskManager = new BukkitTaskManagerImpl(this);
		this.identifierManager = new IdentifierManagerImpl(this).init();
		this.getLogger().info("Loading configuration...");
		this.config = new ConfigImpl(this).init(MiscUtils.mergeArrays(Stream.of(ConfigKey.values()).toArray(AbstractKey[]::new), Stream.of(BukkitConfigKey.values()).toArray(AbstractKey[]::new)));
		this.getLogger().info("Setup locales...");
		Lang.load(new File(this.getDataFolder(), "languages"), this);
		this.getLogger().info("Setup managers...");
		this.channelManager = new ChannelManagerImpl(this);
		this.ordersManager = new OrdersManager(this).init();
		this.userManager = new BukkitUserManagerImpl(this).init();
		this.topManager = new BukkitTopManagerImpl(this).init();
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new GuiListener(), this);
		this.getServer().getPluginManager().registerEvents(new UserListener(this), this);
		this.getLogger().info("Registering commands...");
		try {
			SimpleCommandMap commands = this.getCommandMap();
			PluginCommand walletCmd = this.getPluginCommand(this.getConfiguration().getString(BukkitConfigKey.SHOP_COMMAND_NAME));
			walletCmd.setAliases(this.getConfiguration().getStringList(BukkitConfigKey.SHOP_COMMAND_ALIASES));
			walletCmd.setDescription("Command to open wallet shop gui");
			walletCmd.setExecutor(new WalletCommand(this));
			walletCmd.setPermission("portfel.gui");
			walletCmd.setUsage("/<command>");
			commands.register(this.getName(), walletCmd);
			
			if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				this.papiHandler = new PAPIHandler(this);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.sendMotd();
	}
	
	@Override
	public void onDisable() {
		if (this.userManager != null) this.userManager.killManager();
		if (this.userManager != null) this.topManager.killManager();
		if (this.userManager != null) this.taskManager.cancelAll();
		if (this.userManager != null) this.channelManager.killManager();
		try {
			Field f = Class.forName("net.kyori.adventure.platform.bukkit.BukkitAudiencesImpl").getDeclaredField("INSTANCES");
			f.setAccessible(true);
			Map<?, ?> INSTANCES = (Map<?, ?>) f.get(null);
			INSTANCES.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		try {
			SimpleCommandMap commands = this.getCommandMap();
			commands.getCommands().stream().filter(PluginCommand.class::isInstance).map(PluginCommand.class::cast)
					.filter(cmd -> this.equals(cmd.getPlugin())).forEach(cmd -> cmd.unregister(commands));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		HandlerList.unregisterAll(this);
		
		if(this.papiHandler != null) this.papiHandler.oldUnregister();
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
	
	/**
	 * Get Bukkit audience implementation.
	 * 
	 * @return audiences
	 */
	public @NotNull BukkitAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	
	private SimpleCommandMap getCommandMap() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field f = this.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
		f.setAccessible(true);
		return (SimpleCommandMap) f.get(this.getServer().getPluginManager());
	}
	
	
	private PluginCommand getPluginCommand(String name) {
		try {
			Constructor<PluginCommand> constr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constr.setAccessible(true);
			return constr.newInstance(name, this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void sendMotd() {
		this.getServer().getLogger().info("    \u001b[35m┌───\u001b[35;1m┬───┐\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m└┐┌┐\u001b[35;1m│┌─┐│     \u001b[36;1mPortfel \u001b[35mv3.0.0\u001b[0m");
		this.getServer().getLogger().info("     \u001b[35m│││\u001b[35;1m│└─┘│     \u001b[30;1mRunning on Bukkit - " + this.getServer().getName() + "\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m┌┘└┘\u001b[35;1m│┌──┘\u001b[0m");
		this.getServer().getLogger().info("    \u001b[35m└───\u001b[35;1m┴┘\u001b[0m");
	}
	

}
