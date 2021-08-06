package me.szumielxd.portfel.bukkit.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.Portfel;

public class ChannelManager {
	
	
	private final PortfelBukkit plugin;
	
	private Consumer<BukkitOperableUser> registerer = null;
	
	
	public ChannelManager(@NotNull PortfelBukkit plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_SETUP);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_TRANSACTIONS);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_USERS);
		this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "bungeecord:main", this::onSetupValidator);
		this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_SETUP, this::onSetupChannel);
		this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_TRANSACTIONS, this::onTransactionsChannel);
		this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_USERS, this::onUsersChannel);
	}
	
	
	public void setRegisterer(Consumer<BukkitOperableUser> registerer) {
		if (this.registerer != null) throw new RuntimeException("Registerer is set already.");
		this.registerer = registerer;
	}
	
	
	private final Map<UUID, CompletableFuture<Boolean>> waitingForValidation = new HashMap<>();
	private final Map<UUID, CompletableFuture<BukkitOperableUser>> waitingUserUpdates = new HashMap<>();
	
	
	private void onSetupValidator(@NotNull String channel, @NotNull Player player, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		if ("ForwardToPlayer".equals(subchannel)) {
			byte[] bytes = new byte[in.readShort()];
			in.readFully(bytes);
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
			try {
				String ch = is.readUTF();
				if ("Validate".equals(ch)) {
					UUID operationId = UUID.fromString(is.readUTF()); // operationId
					boolean valid = is.readBoolean();
					CompletableFuture<Boolean> future = this.waitingForValidation.get(operationId);
					if (future != null) {
						future.complete(valid);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void onSetupChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if ("Register".equals(subchannel)) {
			final UUID operationId = UUID.fromString(in.readUTF());
			final UUID proxyId = UUID.fromString(in.readUTF());
			final UUID serverId = UUID.fromString(in.readUTF());
			
			this.plugin.getTaskManager().runTaskAsynchronously(() -> {
				CompletableFuture<Boolean> future = this.waitingForValidation.get(operationId);
				if (future == null) {
					this.waitingForValidation.put(operationId, future = new CompletableFuture<>());
					this.validateSetup(player, operationId);
				}
				Boolean res = null;
				try {
					res = future.get(3, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
				}
				this.waitingForValidation.remove(operationId);
				if (res == true) this.sendSetupChannel(channel, player, operationId, proxyId, serverId);
			});
			
		}
		
	}
	
	private void validateSetup(@NotNull Player player, @NotNull UUID operationId) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ForwardToPlayer"); // subchannel
		out.writeUTF(player.getName()); // player
		out.writeUTF(Portfel.CHANNEL_SETUP); // custom channel
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeUTF("Validate"); // channel
			os.writeUTF(operationId.toString()); // operationId
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.writeShort(baos.toByteArray().length);
		out.write(baos.toByteArray());
		player.sendPluginMessage(plugin, "bungeecord:main", out.toByteArray());
	}
	
	private void sendSetupChannel(@NotNull String channel, @NotNull Player player, @NotNull UUID operationId, @NotNull UUID proxyId, @NotNull UUID serverId) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Register"); // subchannel
		out.writeUTF(operationId.toString()); // operationId
		UUID srvId = this.plugin.getIdentifierManager().getComplementary(proxyId);
		if (srvId != null) {
			out.writeUTF("Set"); // status
		} else {
			out.writeUTF("Ok"); // status
			srvId = serverId;
			this.plugin.getIdentifierManager().register(proxyId, serverId);
		}
		out.writeUTF(proxyId.toString()); // proxyId
		out.writeUTF(srvId.toString()); // serverId
		player.sendPluginMessage(this.plugin, channel, out.toByteArray());
	}
	
	
	private void onTransactionsChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		
	}
	
	
	private void onUsersChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		if ("User".equals(subchannel)) {
			UUID uuid = UUID.fromString(in.readUTF()); // UUID
			BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(uuid);
			boolean online = this.plugin.getServer().getPlayer(uuid) != null;
			if (user == null) {
				String username = in.readUTF(); // username
				long balance = in.readLong(); // balance
				boolean deniedInTop = in.readBoolean(); // deniedInTop
				user = new BukkitOperableUser(this.plugin, uuid, username, online, deniedInTop, balance);
			} else {
				user.setName(in.readUTF()); // username
				user.setPlainBalance(in.readLong()); // balance
				user.setPlainDeniedInTop(in.readBoolean()); // deniedInTop
				user.setOnline(online);
			}
			CompletableFuture<BukkitOperableUser> future = this.waitingUserUpdates.get(uuid);
			if (future != null) {
				future.complete(user);
			}
			this.registerer.accept(user);
		}
	}
	
	
	private void sendUserRequest(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("User");
		player.sendPluginMessage(plugin, Portfel.CHANNEL_USERS, out.toByteArray());
	}
	
	
	public BukkitOperableUser requestPlayer(@NotNull Player player) throws Exception {
		UUID uuid = player.getUniqueId();
		CompletableFuture<BukkitOperableUser> future = this.waitingUserUpdates.get(uuid);
		if (future == null) {
			this.waitingUserUpdates.put(uuid, future = new CompletableFuture<>());
			this.sendUserRequest(player);
		}
		try {
			BukkitOperableUser user = future.get(5, TimeUnit.SECONDS);
			this.waitingUserUpdates.remove(uuid);
			return user;
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			this.waitingUserUpdates.remove(uuid);
			throw e;
		}
	}
	
	
	
	

}
