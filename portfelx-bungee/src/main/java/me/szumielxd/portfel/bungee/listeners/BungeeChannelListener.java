package me.szumielxd.portfel.bungee.listeners;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeePlayer;
import me.szumielxd.portfel.bungee.objects.BungeeServerConnection;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.listeners.ChannelsListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeChannelListener extends ChannelsListener<PortfelBungeeImpl, BaseComponent[]> implements Listener {

	public BungeeChannelListener(@NotNull PortfelBungeeImpl plugin) {
		super(plugin);
	}
	
	
	@EventHandler
	public void onPluginMessageChannel(PluginMessageEvent event) {
		String tag = event.getTag();
		if (isListendChannel(tag) && event.getSender() instanceof Server server && event.getReceiver() instanceof ProxiedPlayer player) {
			ProxyServerConnection<BaseComponent[]> sender = new BungeeServerConnection(this.getPlugin(), server);
			BungeePlayer target = new BungeePlayer(this.getPlugin(), player);
			onPluginMessage(sender, target, tag, event.getData())
					.ifPresent(event::setCancelled);
		}
	}
	

}
