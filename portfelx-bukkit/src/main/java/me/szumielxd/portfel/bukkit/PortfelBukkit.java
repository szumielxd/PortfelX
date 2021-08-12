package me.szumielxd.portfel.bukkit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.bukkit.commands.WalletCommand;
import me.szumielxd.portfel.bukkit.managers.BukkitTaskManager;
import me.szumielxd.portfel.bukkit.managers.BukkitUserManager;
import me.szumielxd.portfel.bukkit.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.managers.IdentifierManager;
import me.szumielxd.portfel.bukkit.managers.OrdersManager;
import me.szumielxd.portfel.common.Config;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Config.ConfigKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.managers.TaskManager;
import me.szumielxd.portfel.common.managers.UserManager;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class PortfelBukkit extends JavaPlugin implements Portfel {
	
	
	private BukkitAudiences adventure;
	private Config config;
	private TaskManager taskManager;
	private IdentifierManager identifierManager;
	private ChannelManager channelManager;
	private OrdersManager ordersManager;
	private UserManager userManager;
	
	
	@Override
	public void onEnable() {
		PortfelProvider.register(this);
		this.adventure = BukkitAudiences.create(this);
		this.taskManager = new BukkitTaskManager(this);
		this.identifierManager = new IdentifierManager(this).init();
		this.config = new Config(this).init(MiscUtils.mergeArrays(ConfigKey.values(), BukkitConfigKey.values()));
		Lang.load(new File(this.getDataFolder(), "languages"), this);
		this.channelManager = new ChannelManager(this);
		this.ordersManager = new OrdersManager(this).init();
		this.userManager = new BukkitUserManager(this).init();
		try {
			SimpleCommandMap commands = this.getCommandMap();
			PluginCommand walletCmd = this.getPluginCommand(this.getConfiguration().getString(BukkitConfigKey.SHOP_COMMAND_NAME));
			walletCmd.setAliases(this.getConfiguration().getStringList(BukkitConfigKey.SHOP_COMMAND_ALIASES));
			walletCmd.setDescription("Command to open wallet shop gui");
			walletCmd.setExecutor(new WalletCommand(this));
			walletCmd.setPermission("portfel.gui");
			walletCmd.setUsage("/<command>");
			walletCmd.register(commands);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		this.userManager.killManager();
		this.taskManager.cancelAll();
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
	}

	@Override
	public @NotNull UserManager getUserManager() {
		return this.userManager;
	}

	@Override
	public @NotNull TaskManager getTaskManager() {
		return this.taskManager;
	}

	@Override
	public @NotNull Config getConfiguration() {
		return this.config;
	}
	
	public @NotNull ChannelManager getChannelManager() {
		return this.channelManager;
	}
	
	public @NotNull IdentifierManager getIdentifierManager() {
		return this.identifierManager;
	}
	
	public @NotNull OrdersManager getOrdersManager() {
		return this.ordersManager;
	}
	
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
	

}
