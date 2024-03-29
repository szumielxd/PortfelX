package me.szumielxd.portfel.velocity.listeners;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.listeners.UserListener;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import me.szumielxd.portfel.velocity.objects.VelocityPlayer;
import me.szumielxd.portfel.velocity.objects.VelocityServerConnection;

public class VelocityUserListener extends UserListener {
	
	
	public VelocityUserListener(@NotNull PortfelVelocityImpl plugin) {
		super(plugin);
	}
	
	
	@Subscribe
	public void onServerConnect(ServerPostConnectEvent event) {
		Optional<ServerConnection> srv = event.getPlayer().getCurrentServer();
		String previousServer = Optional.ofNullable(event.getPreviousServer()).map(s -> s.getServerInfo().getName()).orElse(null);
		ProxyPlayer player = new VelocityPlayer((PortfelVelocityImpl) this.getPlugin(), event.getPlayer());
		this.getPlugin().debug("[%s] Connect: (%s) %s -> %s", "VelocityUserListener", event.getPlayer().getUsername(), previousServer, srv.orElse(null));
		if (srv.isPresent()) {
			this.onConnect(player, new VelocityServerConnection((PortfelVelocityImpl) this.getPlugin(), srv.get()));
		}
	}
	
	
	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		this.getPlugin().debug("[%s] Disconnect: %s", "VelocityUserListener", event.getPlayer().getUsername());
		Player player = event.getPlayer();
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
