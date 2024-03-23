package me.szumielxd.portfel.bungee.listeners;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeePlayer;
import me.szumielxd.portfel.bungee.objects.BungeeServerConnection;
import me.szumielxd.portfel.proxy.listeners.UserListener;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeUserListener extends UserListener<PortfelBungeeImpl, BaseComponent[]> implements Listener {
	
	
	public BungeeUserListener(@NotNull PortfelBungeeImpl plugin) {
		super(plugin);
	}
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onServerConnect(ServerSwitchEvent event) {
		Optional<Server> srv = Optional.ofNullable(event.getPlayer().getServer());
		BungeePlayer player = new BungeePlayer(this.getPlugin(), event.getPlayer());
		if (srv.isPresent()) {
			this.onConnect(player, new BungeeServerConnection(this.getPlugin(), srv.get()));
		}
	}
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDisonnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		try {
			User user = this.getPlugin().getUserManager().getUser(player.getUniqueId());
			if (user != null) {
				((ProxyOperableUser)user).setOnline(false);
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
	

}
