package me.szumielxd.portfel.bukkit.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
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
import com.google.gson.Gson;

import me.szumielxd.portfel.bukkit.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.objects.Transaction;
import me.szumielxd.portfel.bukkit.objects.Transaction.TransactionResult;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.enums.TransactionStatus;
import me.szumielxd.portfel.common.objects.User;

public class ChannelManager {
	
	
	private final PortfelBukkit plugin;
	
	private Consumer<BukkitOperableUser> registerer = null;
	
	
	public ChannelManager(@NotNull PortfelBukkit plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_SETUP);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_TRANSACTIONS);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_USERS);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_BUNGEE);
		this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_BUNGEE, this::onSetupValidator);
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
	private final Map<UUID, Entry<Transaction, CompletableFuture<TransactionResult>>> waitingTransactions = new HashMap<>();
	
	
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
		if (!Portfel.CHANNEL_SETUP.equals(channel)) return;
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
				if (Boolean.TRUE == res) this.sendSetupChannel(channel, player, operationId, proxyId, serverId);
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
		player.sendPluginMessage(plugin, Portfel.CHANNEL_BUNGEE, out.toByteArray());
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
		if (!Portfel.CHANNEL_TRANSACTIONS.equals(channel)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		if ("Buy".equals(subchannel)) {
			UUID proxyId = UUID.fromString(in.readUTF()); // proxy id
			UUID transactionId = UUID.fromString(in.readUTF()); // transaction id
			TransactionResult result = null;
			if (this.plugin.getIdentifierManager().isValid(proxyId)) {
				long newBalance = in.readLong(); // newBalance
				Optional<TransactionStatus> status = TransactionStatus.parse(in.readUTF()); // status
				if (status.isPresent()) {
					int globalOrders = 0;
					Throwable throwable = null;
					if (status.get().equals(TransactionStatus.OK)) {
						globalOrders = in.readInt();
					} else if (status.get().equals(TransactionStatus.OK)) {
						try {
							throwable = new Gson().fromJson(in.readUTF(), Throwable.class);
						} catch (Exception e) {
							throwable = e;
						}
					}
					result = new TransactionResult(transactionId, status.get(), newBalance, globalOrders, throwable);
				}
			}
			Entry<Transaction, CompletableFuture<TransactionResult>> entry = this.waitingTransactions.get(transactionId);
			if (entry == null) return;
			if (!proxyId.equals(((BukkitOperableUser)entry.getKey().getUser()).getProxyId())) result = null;
			entry.getValue().complete(result);
		}
	}
	
	
	private void onUsersChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		if (!Portfel.CHANNEL_USERS.equals(channel)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		if ("User".equals(subchannel)) {
			BukkitOperableUser user = null;
			UUID proxyId = UUID.fromString(in.readUTF()); // proxyId
			UUID uuid = UUID.fromString(in.readUTF()); // UUID
			if (this.plugin.getIdentifierManager().isValid(proxyId)) {
				user = (BukkitOperableUser) this.plugin.getUserManager().getUser(uuid);
				boolean online = this.plugin.getServer().getPlayer(uuid) != null;
				if (user == null) {
					String username = in.readUTF(); // username
					long balance = in.readLong(); // balance
					boolean deniedInTop = in.readBoolean(); // deniedInTop
					user = new BukkitOperableUser(this.plugin, uuid, username, online, deniedInTop, balance, proxyId);
				} else {
					user.setName(in.readUTF()); // username
					user.setPlainBalance(in.readLong()); // balance
					user.setPlainDeniedInTop(in.readBoolean()); // deniedInTop
					user.setOnline(online);
				}
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
	
	
	private void sendTransaction(Player player, Transaction transaction) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Buy"); // subchannel
		out.writeUTF(this.plugin.getIdentifierManager().getComplementary(((BukkitOperableUser)transaction.getUser()).getProxyId()).toString()); // serverId
		out.writeUTF(transaction.getTransactionId().toString()); // transactionId
		out.writeUTF(this.plugin.getConfiguration().getString(BukkitConfigKey.SERVER_NAME)); // server
		out.writeLong(transaction.getOrder().getPrice()); // value
		out.writeUTF(this.plugin.getName()); // plugin
		out.writeUTF(transaction.getOrder().getName()); // order
		player.sendPluginMessage(plugin, Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	
	
	public @NotNull Transaction requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order) {
		UUID transactionId = UUID.randomUUID();
		try {
			User user = this.plugin.getUserManager().getOrLoadUser(player.getUniqueId());
			if (user == null) return null;
			Transaction trans = new Transaction(this.plugin, user, transactionId, order);
			CompletableFuture<TransactionResult> future = new CompletableFuture<>();
			this.waitingTransactions.put(transactionId, new SimpleEntry<>(trans, future));
			this.sendTransaction(player, trans);
			try {
				TransactionResult result = future.get(5, TimeUnit.SECONDS);
				this.waitingUserUpdates.remove(transactionId);
				trans.finish(result);
				return trans;
			} catch (ExecutionException | InterruptedException | TimeoutException e) {
				this.waitingUserUpdates.remove(transactionId);
				throw e;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	
	

}
