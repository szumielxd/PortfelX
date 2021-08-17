package me.szumielxd.portfel.bungee.api.objects;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeActionExecutor extends ActionExecutor {

	
	private BungeeActionExecutor(@NotNull String displayName, @NotNull UUID uuid) {
		super(displayName, uuid);
	}
	
	
	/**
	 * Create ActionExecutor from player.
	 * 
	 * @param player player to get data from
	 * @return ActionExecutor based on given player
	 */
	public static @NotNull BungeeActionExecutor player(@NotNull ProxiedPlayer player) {
		return new BungeeActionExecutor(player.getName(), player.getUniqueId());
	}
	
	/**
	 * Create ActionExecutor from plugin
	 * 
	 * @param plugin plugin to get data from
	 * @return ActionExecutor based on given plugin
	 */
	public static @NotNull BungeeActionExecutor plugin(@NotNull Plugin plugin) {
		return new BungeeActionExecutor(plugin.getDescription().getName(), ActionExecutor.PLUGIN_UUID);
	}
	
	/**
	 * Create ActionExecutor from pluginName
	 * 
	 * @param pluginName name of plugin used to create ActionExecutor
	 * @return ActionExecutor based on given pluginName
	 */
	public static @NotNull BungeeActionExecutor plugin(@NotNull String pluginName) {
		return new BungeeActionExecutor(pluginName, ActionExecutor.PLUGIN_UUID);
	}
	
	/**
	 * Create ActionExecutor from console instance.
	 * 
	 * @return ActionExecutor representation of console
	 */
	public static @NotNull BungeeActionExecutor console() {
		return new BungeeActionExecutor("Console", ActionExecutor.CONSOLE_UUID);
	}
	
	/**
	 * Create ActionExecutor from console instance.
	 * 
	 * @return ActionExecutor representation of console
	 */
	public static @NotNull BungeeActionExecutor sender(CommonSender sender) {
		if (sender instanceof CommonPlayer) {
			return new BungeeActionExecutor(sender.getName(), ((CommonPlayer)sender).getUniqueId());
		}
		return new BungeeActionExecutor("Console", ActionExecutor.CONSOLE_UUID);
	}

}
