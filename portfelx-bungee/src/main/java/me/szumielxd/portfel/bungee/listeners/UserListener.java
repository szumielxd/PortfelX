package me.szumielxd.portfel.bungee.listeners;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.objects.BungeeOperableUser;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.objects.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class UserListener implements Listener {
	
	
	private final PortfelBungee plugin;
	
	
	public UserListener(@NotNull PortfelBungee plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onConnect(ServerSwitchEvent event) {
		Server srv = event.getPlayer().getServer();
		ProxiedPlayer player = event.getPlayer();
		this.plugin.getTaskManager().runTaskAsynchronously(() -> {
			try {
				User user = this.plugin.getUserManager().getOrCreateUser(player.getUniqueId());
				if (srv != player.getServer()) return;
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("User");
				out.writeUTF(this.plugin.getProxyId().toString());
				out.writeUTF(player.getUniqueId().toString());
				out.writeUTF(player.getName());
				out.writeLong(user.getBalance());
				out.writeBoolean(user.isDeniedInTop());
				srv.sendData(Portfel.CHANNEL_USERS, out.toByteArray());
			} catch (Exception e) {	
				e.printStackTrace();
			}
		});
	}
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onConnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		try {
			User user = this.plugin.getUserManager().getUser(player.getUniqueId());
			if (user != null) {
				((BungeeOperableUser)user).setOnline(false);
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
	

}
