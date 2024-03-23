package me.szumielxd.portfel.bungee.managers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeePlayer;
import me.szumielxd.portfel.bungee.objects.BungeeServerConnection;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeAccessManagerImpl extends AccessManagerImpl implements Listener {

	public BungeeAccessManagerImpl(@NotNull PortfelBungeeImpl plugin) {
		super(plugin);
	}

	@Override
	protected void preInit() {
		// empty
	}

	@Override
	protected void postInit() {
		((PortfelBungeeImpl) this.getPlugin()).asPlugin().getProxy().getPluginManager().registerListener(((PortfelBungeeImpl) this.getPlugin()).asPlugin(), this);;		
	}
	
	
	@EventHandler
	public void onPluginMessageChannel(PluginMessageEvent event) {
		String tag = event.getTag();
		if (this.isListendChannel(tag) && event.getSender() instanceof Server server && event.getReceiver() instanceof ProxiedPlayer player) {
			ProxyServerConnection sender = new BungeeServerConnection((PortfelBungeeImpl) this.getPlugin(), server);
			BungeePlayer target = new BungeePlayer((PortfelBungeeImpl) this.getPlugin(), player);
			Optional<Boolean> result = this.onPluginMessage(sender, target, tag, event.getData());
			if (result.isPresent()) {
				event.setCancelled(result.get());
			}
		}
	}
	

}
