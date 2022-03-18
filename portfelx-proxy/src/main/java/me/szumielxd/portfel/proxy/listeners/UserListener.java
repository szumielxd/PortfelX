package me.szumielxd.portfel.proxy.listeners;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public abstract class UserListener {
	
	
	private final PortfelProxyImpl plugin;
	
	
	public UserListener(@NotNull PortfelProxyImpl plugin) {
		this.plugin = plugin;
	}
	
	
	protected final PortfelProxyImpl getPlugin() {
		return this.plugin;
	}
	

	
	protected void onConnect(@NotNull ProxyPlayer player, @NotNull ProxyServerConnection server) {
		this.plugin.debug("UserListener::onConnect(%s, %s)", player, server);
		this.plugin.getTaskManager().runTaskAsynchronously(() -> {
			try {
				ProxyOperableUser user = (ProxyOperableUser) this.plugin.getUserManager().getOrCreateUser(player.getUniqueId());
				user.setRemoteIdAndName(null, null);
				if (!player.getServer().filter(server::equals).isPresent()) return;
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("User");
				out.writeUTF(this.plugin.getProxyId().toString());
				out.writeUTF(player.getUniqueId().toString());
				out.writeUTF(player.getName());
				out.writeLong(user.getBalance());
				out.writeBoolean(user.isDeniedInTop());
				server.sendPluginMessage(Portfel.CHANNEL_USERS, out.toByteArray());
				
			} catch (Exception e) {	
				e.printStackTrace();
			}
		});
	}
	

}
