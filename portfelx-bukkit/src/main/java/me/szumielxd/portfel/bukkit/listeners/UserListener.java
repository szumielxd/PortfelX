package me.szumielxd.portfel.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;

public class UserListener implements Listener {
	
	
	private final PortfelBukkitImpl plugin;
	
	
	public UserListener(PortfelBukkitImpl plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
		if (user != null) {
			user.setOnline(false);
		}
		this.plugin.getChannelManager().ensureNotTopRequestSource(event.getPlayer());
		this.plugin.getChannelManager().ensureNotUserUpdating(event.getPlayer());
	}
	

}
