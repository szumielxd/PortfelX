package me.szumielxd.portfel.bungee.listeners;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import me.szumielxd.portfel.bungee.objects.BungeeOperableUser;
import me.szumielxd.portfel.common.utils.CryptoUtils;
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
					this.plugin.getTaskManager().runTaskAsynchronously(() -> {
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
					});
				}
			}
		}
	}
	
	
	@EventHandler
	public void onServerData(PluginMessageEvent event) {
		if (Portfel.CHANNEL_USERS.equals(event.getTag())) {
			event.setCancelled(true);
			if (event.getSender() instanceof Server) {
				ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
				String subchannel = in.readUTF();
				if ("ServerId".equals(subchannel)) {
					try {
						ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
						BungeeOperableUser user = (BungeeOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
						if (user != null) {
							UUID uuid = UUID.fromString(in.readUTF());
							if (this.plugin.getAccessManager().canAccess(uuid)) {
								user.setRemoteIdAndName(uuid, in.readUTF());
							}
						}
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
					byte[] data;
					try {
						data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getAccessManager().getHashKey(serverKey));
					} catch (IllegalArgumentException e) {
						// ignore malformed messages
						return;
					}
					try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
						final String transactionId = din.readUTF(); // transaction id
						String server = din.readUTF(); // server
						long value = din.readLong(); // value
						ActionExecutor pluginExecutor = BungeeActionExecutor.plugin(din.readUTF()); // plugin
						String order = din.readUTF(); // order
						this.runTransaction(transactionId, serverKey, srv, player, pluginExecutor, order, server, value);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	private void runTransaction(@NotNull String transactionId, @NotNull UUID serverId, @NotNull Server srv, @NotNull ProxiedPlayer target, @NotNull ActionExecutor pluginExecutor, @NotNull String order, @NotNull String server, long value) {
		User user = this.plugin.getUserManager().getUser(target.getUniqueId());
		CompletableFuture<Exception> future = user.takeBalance(value, pluginExecutor, server, order);
		this.plugin.getTaskManager().runTask(() -> {
			try {
				Exception ex = future.get();
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Buy"); // subchannel
				try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
						DataOutputStream dout = new DataOutputStream(bout);) {
					dout.writeUTF(this.plugin.getProxyId().toString()); // proxy id
					dout.writeUTF(transactionId); // transaction id
					dout.writeLong(user.getBalance()); // new balance
					int found = (int) this.plugin.getOrdersManager().getOrders().values().stream()
							.filter(o -> o.examine(user, order)).count();
					if (ex == null) {
						dout.writeUTF(TransactionStatus.OK.getText()); // status
						dout.writeInt(found);
					} else {
						dout.writeUTF(TransactionStatus.ERROR.getText());
						dout.writeUTF(this.gson.toJson(this.gson.toJsonTree(ex))); // status
					}
					CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getAccessManager().getHashKey(serverId));
				} catch (IOException e) {
					e.printStackTrace();
				}
				srv.sendData(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
	}
	

}
