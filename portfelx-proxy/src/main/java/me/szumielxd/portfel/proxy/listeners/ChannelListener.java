package me.szumielxd.portfel.proxy.listeners;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.PluginMessageTarget;

public abstract class ChannelListener {
	
	
	private final PortfelProxyImpl plugin;
	private final Gson gson;
	
	
	protected ChannelListener(@NotNull PortfelProxyImpl plugin) {
		this.plugin = plugin;
		this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	}
	
	
	protected @NotNull PortfelProxyImpl getPlugin() {
		return this.plugin;
	}
	
	
	protected final boolean isListendChannel(@Nullable String tag) {
		return Portfel.CHANNEL_SETUP.equals(tag)
				|| Portfel.CHANNEL_TRANSACTIONS.equals(tag)
				|| Portfel.CHANNEL_USERS.equals(tag);
	}
	
	
	protected final Optional<Boolean> onPluginMessage(@NotNull PluginMessageTarget sender, @NotNull PluginMessageTarget target, @NotNull String tag, byte[] message) {
		if (sender instanceof ProxyServerConnection && target instanceof ProxyPlayer) {
			ProxyServerConnection server = (ProxyServerConnection) sender;
			ProxyPlayer player = (ProxyPlayer) target;
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			
			if (Portfel.CHANNEL_USERS.equals(tag)) {
				switch (subchannel) {
					case "User": return Optional.of(this.onUserData(server, player, tag, subchannel, in));
					case "ServerId": return Optional.of(this.onServerData(server, player, tag, subchannel, in));
					case "Top": return Optional.of(this.onTopData(server, player, tag, subchannel, in));
					case "LightTop": return Optional.of(this.onLightTopData(server, player, tag, subchannel, in));
					case "MinorTop": return Optional.of(this.onMinorTopData(server, player, tag, subchannel, in));
					default: break;
				}
			}
			if (Portfel.CHANNEL_TRANSACTIONS.equals(tag)) {
				switch (subchannel) {
					case "Buy": return Optional.of(this.onTransaction(server, player, tag, subchannel, in));
					case "MinorTake": return Optional.of(this.onMinorEcoTake(server, player, tag, subchannel, in));
					case "MinorGive": return Optional.of(this.onMinorEcoGive(server, player, tag, subchannel, in));
					default: break;
				}
			}
		}
		return Optional.of(true);
	}
	
	
	// user channel
	private boolean onUserData(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		this.plugin.getTaskManager().runTaskAsynchronously(() -> {
			try {
				User user = this.plugin.getUserManager().getOrCreateUser(target.getUniqueId());
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF(subchannel);
				out.writeUTF(this.plugin.getProxyId().toString());
				out.writeUTF(target.getUniqueId().toString());
				out.writeUTF(target.getName());
				out.writeLong(user.getBalance());
				out.writeBoolean(user.isDeniedInTop());
				out.writeLong(user.getMinorBalance());
				sender.sendPluginMessage(tag, out.toByteArray());
			} catch (Exception e) {	
				e.printStackTrace();
			}
		});
		return true;
	}
	
	
	// user channel
	private boolean onServerData(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
			ProxyOperableUser user = (ProxyOperableUser) this.plugin.getUserManager().getUser(target.getUniqueId());
			if (user != null) {
				UUID uuid = UUID.fromString(in.readUTF());
				if (this.plugin.getAccessManager().canAccess(uuid)) {
					user.setRemoteIdAndName(uuid, in.readUTF());
				}
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return true;
	}
	
	
	// user channel
	// Legacy format
	private boolean onTopData(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
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
			
			sender.sendPluginMessage(tag, out.toByteArray());
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return true;
	}
	
	
	// user channel
	private boolean onLightTopData(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(subchannel);
			
			// proxyId
			out.writeLong(this.plugin.getProxyId().getMostSignificantBits());
			out.writeLong(this.plugin.getProxyId().getLeastSignificantBits());
			
			List<TopEntry> list = this.plugin.getTopManager().getFullTopCopy();
			out.writeInt(list.size());
			list.forEach(top -> {
				//uuid
				out.writeLong(top.getUniqueId().getMostSignificantBits());
				out.writeLong(top.getUniqueId().getLeastSignificantBits());
				
				//username
				IntStream.range(0, 16).forEach(i -> out.write((byte)((top.getName().length() > i ? top.getName().charAt(i) : ' ') - 128)));
				
				//balance
				out.writeLong(top.getBalance());
			});
			
			sender.sendPluginMessage(tag, out.toByteArray());
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return true;
	}
	
	
	// user channel
	private boolean onMinorTopData(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(subchannel);
			
			// proxyId
			out.writeLong(this.plugin.getProxyId().getMostSignificantBits());
			out.writeLong(this.plugin.getProxyId().getLeastSignificantBits());
			
			List<TopEntry> list = this.plugin.getTopManager().getFullMinorTopCopy();
			out.writeInt(list.size());
			list.forEach(top -> {
				//uuid
				out.writeLong(top.getUniqueId().getMostSignificantBits());
				out.writeLong(top.getUniqueId().getLeastSignificantBits());
				
				//username
				IntStream.range(0, 16).forEach(i -> out.write((byte)((top.getName().length() > i ? top.getName().charAt(i) : ' ') - 128)));
				
				//balance
				out.writeLong(top.getBalance());
			});
			
			sender.sendPluginMessage(tag, out.toByteArray());
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return true;
	}
	
	
	// transaction channel
	private boolean onTransaction(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		UUID serverKey = UUID.fromString(in.readUTF()); // serverKey
		if (!this.plugin.getAccessManager().canAccess(serverKey)) return true;
		byte[] data;
		try {
			data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getAccessManager().getHashKey(serverKey));
		} catch (IllegalArgumentException e) {
			// ignore malformed messages
			return true;
		}
		try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
			final String transactionId = din.readUTF(); // transaction id
			String server = din.readUTF(); // server
			long value = din.readLong(); // value
			ActionExecutor pluginExecutor = ProxyActionExecutor.plugin(din.readUTF()); // plugin
			String order = din.readUTF(); // order
			this.runTransaction(transactionId, serverKey, sender, target, pluginExecutor, order, server, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoGive(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		UUID serverKey = UUID.fromString(in.readUTF()); // serverKey
		if (!this.plugin.getAccessManager().canAccess(serverKey)
				|| !this.plugin.getAccessManager().canAccess(serverKey, "minorbalance:give")) return true;
		byte[] data;
		try {
			data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getAccessManager().getHashKey(serverKey));
		} catch (IllegalArgumentException e) {
			// ignore malformed messages
			return true;
		}
		try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
			final String transactionId = din.readUTF(); // transaction id
			long value = din.readLong(); // value
			this.setNewMinorEconomy(sender, transactionId, serverKey, target, tag, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoTake(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		UUID serverKey = UUID.fromString(in.readUTF()); // serverKey
		if (!this.plugin.getAccessManager().canAccess(serverKey)
				|| !this.plugin.getAccessManager().canAccess(serverKey, "minorbalance:take")) return true;
		byte[] data;
		try {
			data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getAccessManager().getHashKey(serverKey));
		} catch (IllegalArgumentException e) {
			// ignore malformed messages
			return true;
		}
		try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
			final String transactionId = din.readUTF(); // transaction id
			long value = din.readLong(); // value
			this.setNewMinorEconomy(sender, transactionId, serverKey, target, tag, -value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void setNewMinorEconomy(@NotNull ProxyServerConnection sender, @NotNull String transactionId, @NotNull UUID serverId, @NotNull ProxyPlayer target, @NotNull String tag, long value) throws IOException {
		ProxyOperableUser user = (ProxyOperableUser) this.plugin.getUserManager().getUser(target.getUniqueId());
		boolean result = false;
		if (user != null) {
			synchronized (user) {
				if (user.getBalance() > -value) {
					user.setMinorBalance(user.getBalance() + value);
					result = true;
				}
			}
		}
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(tag);
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(bout);) {
			dout.writeUTF(this.plugin.getProxyId().toString()); // proxy id
			dout.writeUTF(transactionId); // transaction id
			dout.writeBoolean(result);
			if (result) dout.writeLong(user.getBalance());
			CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getAccessManager().getHashKey(serverId));
		}
		sender.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	
	
	private void runTransaction(@NotNull String transactionId, @NotNull UUID serverId, @NotNull ProxyServerConnection srv, @NotNull ProxyPlayer target, @NotNull ActionExecutor pluginExecutor, @NotNull String order, @NotNull String server, long value) {
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
					int found = (int) this.plugin.getOrdersManager().getOrders().values().stream().filter(o -> this.plugin.getAccessManager().canAccess(serverId, o.getName()))
							.filter(o -> o.examine(user, order)).count();
					if (ex == null) {
						dout.writeUTF(TransactionStatus.OK.getText()); // status
						dout.writeInt(found);
					} else {
						dout.writeUTF(TransactionStatus.ERROR.getText());
						dout.writeUTF(this.gson.toJson(this.gson.toJsonTree(ex))); // status
					}
					CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getAccessManager().getHashKey(serverId));
				}
				srv.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	

}
