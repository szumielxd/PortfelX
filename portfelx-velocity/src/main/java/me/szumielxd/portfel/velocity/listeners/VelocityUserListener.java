package me.szumielxd.portfel.velocity.listeners;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.listeners.UserListener;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import me.szumielxd.portfel.velocity.objects.VelocityPlayer;
import me.szumielxd.portfel.velocity.objects.VelocityServerConnection;
import net.kyori.adventure.text.Component;

public class VelocityUserListener extends UserListener<PortfelVelocityImpl, Component> {
	
	
	public VelocityUserListener(@NotNull PortfelVelocityImpl plugin) {
		super(plugin);
	}
	
	
	@Subscribe
	public void onServerConnect(ServerPostConnectEvent event) {
		Optional<ServerConnection> srv = event.getPlayer().getCurrentServer();
		String previousServer = Optional.ofNullable(event.getPreviousServer()).map(s -> s.getServerInfo().getName()).orElse(null);
		ProxyPlayer<Component> player = new VelocityPlayer(getPlugin(), event.getPlayer());
		getPlugin().debug("[%s] Connect: (%s) %s -> %s", "VelocityUserListener", event.getPlayer().getUsername(), previousServer, srv.orElse(null));
		if (srv.isPresent()) {
			onConnect(player, new VelocityServerConnection(getPlugin(), srv.get()));
		}
	}
	
	
	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		getPlugin().debug("[%s] Disconnect: %s", "VelocityUserListener", event.getPlayer().getUsername());
		Player player = event.getPlayer();
		try {
			var user = getPlugin().getUserManager().getUser(player.getUniqueId());
			if (user != null) {
				user.setOnline(false);
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
	

}
