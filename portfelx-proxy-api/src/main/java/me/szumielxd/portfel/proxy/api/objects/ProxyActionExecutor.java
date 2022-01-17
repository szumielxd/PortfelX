package me.szumielxd.portfel.proxy.api.objects;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.proxy.api.PortfelProxy;

public final class ProxyActionExecutor extends ActionExecutor {

	
	private ProxyActionExecutor(@NotNull String displayName, @NotNull UUID uuid) {
		super(displayName, uuid);
	}
	
	
	/**
	 * Create ActionExecutor from player.
	 * 
	 * @param player player to get data from
	 * @return ActionExecutor based on given player
	 */
	public static @NotNull ProxyActionExecutor player(@NotNull ProxyPlayer player) {
		return new ProxyActionExecutor(player.getName(), player.getUniqueId());
	}
	
	/**
	 * Create ActionExecutor from plugin
	 * 
	 * @param plugin plugin to get data from
	 * @return ActionExecutor based on given plugin
	 */
	public static @NotNull ProxyActionExecutor plugin(@NotNull PortfelProxy plugin) {
		return new ProxyActionExecutor(plugin.getName(), ActionExecutor.PLUGIN_UUID);
	}
	
	/**
	 * Create ActionExecutor from pluginName
	 * 
	 * @param pluginName name of plugin used to create ActionExecutor
	 * @return ActionExecutor based on given pluginName
	 */
	public static @NotNull ProxyActionExecutor plugin(@NotNull String pluginName) {
		return new ProxyActionExecutor(pluginName, ActionExecutor.PLUGIN_UUID);
	}
	
	/**
	 * Create ActionExecutor from console instance.
	 * 
	 * @return ActionExecutor representation of console
	 */
	public static @NotNull ProxyActionExecutor console() {
		return new ProxyActionExecutor("Console", ActionExecutor.CONSOLE_UUID);
	}
	
	/**
	 * Create ActionExecutor from console instance.
	 * 
	 * @return ActionExecutor representation of console
	 */
	public static @NotNull ProxyActionExecutor sender(CommonSender sender) {
		if (sender instanceof CommonPlayer) {
			return new ProxyActionExecutor(sender.getName(), ((CommonPlayer)sender).getUniqueId());
		}
		return new ProxyActionExecutor("Console", ActionExecutor.CONSOLE_UUID);
	}

}
