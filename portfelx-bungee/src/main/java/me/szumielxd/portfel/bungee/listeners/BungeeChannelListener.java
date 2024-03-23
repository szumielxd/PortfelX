package me.szumielxd.portfel.bungee.listeners;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeePlayer;
import me.szumielxd.portfel.bungee.objects.BungeeServerConnection;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.listeners.ChannelListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeChannelListener extends ChannelListener<PortfelBungeeImpl, BaseComponent[]> implements Listener {

	public BungeeChannelListener(@NotNull PortfelBungeeImpl plugin) {
		super(plugin);
	}
	
	
	@EventHandler
	public void onPluginMessageChannel(PluginMessageEvent event) {
		String tag = event.getTag();
		if (this.isListendChannel(tag) && event.getSender() instanceof Server server && event.getReceiver() instanceof ProxiedPlayer player) {
			ProxyServerConnection sender = new BungeeServerConnection(this.getPlugin(), server);
			BungeePlayer target = new BungeePlayer(this.getPlugin(), player);
			Optional<Boolean> result = this.onPluginMessage(sender, target, tag, event.getData());
			if (result.isPresent()) {
				event.setCancelled(result.get());
			}
		}
	}
	

}
