package me.szumielxd.portfel.bungee.listeners;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.objects.BungeeActionExecutor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChannelListener implements Listener {
	
	
	private final PortfelBungeeImpl plugin;
	private final Gson gson;
	
	
	public ChannelListener(@NotNull PortfelBungeeImpl plugin) {
		this.plugin = plugin;
		this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	}
	
	
	@EventHandler
	public void onUserData(PluginMessageEvent event) {
		if (Portfel.CHANNEL_USERS.equals(event.getTag())) {
			event.setCancelled(true);
			if (event.getSender() instanceof Server) {
				ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
				String subchannel = in.readUTF();
				if ("User".equals(subchannel)) {
					try {
						Server srv = (Server) event.getSender();
						ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
						User user = this.plugin.getUserManager().getOrCreateUser(player.getUniqueId());
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF(subchannel);
						out.writeUTF(this.plugin.getProxyId().toString());
						out.writeUTF(player.getUniqueId().toString());
						out.writeUTF(player.getName());
						out.writeLong(user.getBalance());
						out.writeBoolean(user.isDeniedInTop());
						srv.sendData(event.getTag(), out.toByteArray());
					} catch (Exception e) {	
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onTopData(PluginMessageEvent event) {
		if (Portfel.CHANNEL_USERS.equals(event.getTag())) {
			event.setCancelled(true);
			if (event.getSender() instanceof Server) {
				ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
				String subchannel = in.readUTF();
				if ("Top".equals(subchannel)) {
					try {
						Server srv = (Server) event.getSender();
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF(subchannel);
						out.writeUTF(this.plugin.getProxyId().toString());
						
						List<TopEntry> list = this.plugin.getTopManager().getFullTopCopy();
						out.writeInt(list.size());
						list.forEach(top -> {
							out.writeUTF(top.getUniqueId().toString());
							out.writeUTF(top.getName());
							out.writeLong(top.getBalance());
						});
						
						srv.sendData(event.getTag(), out.toByteArray());
					} catch (Exception e) {	
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onTransaction(PluginMessageEvent event) {
		if (Portfel.CHANNEL_TRANSACTIONS.equals(event.getTag())) {
			event.setCancelled(true);
			if (event.getSender() instanceof Server) {
				Server srv = (Server) event.getSender();
				ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
				ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
				String subchannel = in.readUTF();
				if ("Buy".equals(subchannel)) {
					UUID serverKey = UUID.fromString(in.readUTF()); // serverKey
					if (!this.plugin.getAccessManager().canAccess(serverKey)) return;
					final String transactionId = in.readUTF(); // transaction id
					String server = in.readUTF(); // server
					long value = in.readLong(); // value
					ActionExecutor plugin = BungeeActionExecutor.plugin(in.readUTF()); // plugin
					String order = in.readUTF(); // order
					User user = this.plugin.getUserManager().getUser(player.getUniqueId());
					CompletableFuture<Exception> future = user.takeBalance(value, plugin, server, order);
					this.plugin.getTaskManager().runTask(() -> {
						try {
							Exception ex = future.get();
							ByteArrayDataOutput out = ByteStreams.newDataOutput();
							out.writeUTF(subchannel); // subchannel
							out.writeUTF(this.plugin.getProxyId().toString()); // proxy id
							out.writeUTF(transactionId); // transaction id
							out.writeLong(user.getBalance()); // new balance
							int found = (int) this.plugin.getOrdersManager().getOrders().values().stream()
									.filter(o -> o.examine(user, order)).count();
							if (ex == null) {
								out.writeUTF(TransactionStatus.OK.getText()); // status
								out.writeInt(found);
							} else {
								out.writeUTF(TransactionStatus.ERROR.getText());
								out.writeUTF(this.gson.toJson(this.gson.toJsonTree(srv))); // status
							}
							srv.sendData(event.getTag(), out.toByteArray());
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					});
				}
			}
		}
	}
	

}
