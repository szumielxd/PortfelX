package me.szumielxd.portfel.bukkit.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;
import me.szumielxd.portfel.bukkit.api.objects.Transaction.TransactionResult;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.TransactionImpl;
import me.szumielxd.portfel.common.utils.CryptoUtils;

public class ChannelManagerImpl implements ChannelManager {
	
	
	private final PortfelBukkitImpl plugin;
	
	private Consumer<BukkitOperableUser> registerer = null;
	private final PluginMessageListenerRegistration bungee;
	private final PluginMessageListenerRegistration setup;
	private final PluginMessageListenerRegistration transactions;
	private final PluginMessageListenerRegistration users;
	private String BUNGEE_CHANNEL;
	
	
	public ChannelManagerImpl(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_SETUP);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_TRANSACTIONS);
		this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Portfel.CHANNEL_USERS);
		try {
			this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEE_CHANNEL = Portfel.CHANNEL_LEGACY_BUNGEE);
		} catch (IllegalArgumentException e) { // fallback to new BungeeCord channel
			this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEE_CHANNEL = Portfel.CHANNEL_BUNGEE);
		}
		this.bungee = this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BUNGEE_CHANNEL, this::onSetupValidator);
		this.setup = this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_SETUP, this::onSetupChannel);
		this.transactions = this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_TRANSACTIONS, this::onTransactionsChannel);
		this.users = this.plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Portfel.CHANNEL_USERS, this::onUsersChannel);
	}
	
	
	public void killManager() {
		this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, BUNGEE_CHANNEL, this.bungee.getListener());
		this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, Portfel.CHANNEL_SETUP, this.setup.getListener());
		this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, Portfel.CHANNEL_TRANSACTIONS, this.transactions.getListener());
		this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin, Portfel.CHANNEL_USERS, this.users.getListener());
	}
	
	
	public void setRegisterer(Consumer<BukkitOperableUser> registerer) {
		if (this.registerer != null) throw new RuntimeException("Registerer is set already.");
		this.registerer = registerer;
	}
	
	
	private final Map<UUID, CompletableFuture<Boolean>> waitingForValidation = new HashMap<>();
	private final Map<UUID, CompletableFuture<List<TopEntry>>> waitingTopUpdates = new ConcurrentHashMap<>();
	private final Map<UUID, UUID> topUpdatesByUser = new HashMap<>();
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
		
		this.plugin.debug("PluginMessage(setup|%s)\\[%s\\]: %s", player.getName(), message.length, subchannel);
		
		if ("Register".equals(subchannel)) {
			byte[] data;
			try {
				data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getServerHashKey());
			} catch (IllegalArgumentException e) {
				// ignore malformed message
				return;
			}
			
			try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
				final UUID operationId = UUID.fromString(din.readUTF());
				final UUID proxyId = UUID.fromString(din.readUTF());
				final UUID serverId = UUID.fromString(din.readUTF());
				
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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
		player.sendPluginMessage(plugin, BUNGEE_CHANNEL, out.toByteArray());
	}
	
	private void sendSetupChannel(@NotNull String channel, @NotNull Player player, @NotNull UUID operationId, @NotNull UUID proxyId, @NotNull UUID serverId) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Register"); // subchannel
		out.writeUTF(operationId.toString()); // operationId
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(bout);) {
			
			UUID srvId = this.plugin.getIdentifierManager().getComplementary(proxyId);
			if (srvId != null) {
				dout.writeUTF("Set"); // status
			} else {
				dout.writeUTF("Ok"); // status
				srvId = serverId;
				this.plugin.getIdentifierManager().register(proxyId, serverId);
			}
			dout.writeUTF(proxyId.toString()); // proxyId
			dout.writeUTF(srvId.toString()); // serverId
			CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getServerHashKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		player.sendPluginMessage(this.plugin, channel, out.toByteArray());
	}
	
	
	private void onTransactionsChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		if (!Portfel.CHANNEL_TRANSACTIONS.equals(channel)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		
		this.plugin.debug("PluginMessage(transaction|%s)\\[%s\\]: %s", player.getName(), message.length, subchannel);
		
		/* Handle shipping */
		if ("Buy".equals(subchannel)) {
			byte[] data;
			try {
				data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getServerHashKey());
			} catch (IllegalArgumentException e) {
				// ignore malformed messages
				return;
			}
			try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
				UUID proxyId = UUID.fromString(din.readUTF()); // proxy id
				UUID transactionId = UUID.fromString(din.readUTF()); // transaction id
				TransactionResult result = null;
				if (this.plugin.getIdentifierManager().isValid(proxyId)) {
					long newBalance = din.readLong(); // newBalance
					Optional<TransactionStatus> status = TransactionStatus.parse(din.readUTF()); // status
					if (status.isPresent()) {
						int globalOrders = 0;
						Throwable throwable = null;
						if (status.get().equals(TransactionStatus.OK)) {
							globalOrders = din.readInt();
						} else if (status.get().equals(TransactionStatus.OK)) {
							try {
								throwable = new Gson().fromJson(din.readUTF(), Throwable.class);
							} catch (Exception e) {
								throwable = e;
							}
						}
						result = new TransactionResult(transactionId, status.get(), newBalance, globalOrders, throwable);
					}
				}
				Entry<Transaction, CompletableFuture<TransactionResult>> entry = this.waitingTransactions.get(transactionId);
				if (entry == null) return;
				if (!proxyId.equals(entry.getKey().getUser().getRemoteId())) result = null;
				entry.getValue().complete(result);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		
		/* Handle TokenPrizes */
		if ("Token".equals(subchannel)) {
			byte[] data;
			try {
				data = CryptoUtils.decodeBytesFromInput(in, this.plugin.getServerHashKey());
			} catch (IllegalArgumentException e) {
				// ignore malformed messages
				return;
			}
			try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
				UUID proxyId = UUID.fromString(din.readUTF()); // proxyId
				UUID serverId = UUID.fromString(din.readUTF()); // serverId
				String token = din.readUTF(); // token
				String order = din.readUTF(); // orderName
				long globalOrders = din.readLong(); // globalOrdersCount
				if (serverId.equals(this.plugin.getIdentifierManager().getComplementary(proxyId))) {
					User user = this.plugin.getUserManager().getUser(player.getUniqueId());
					if (user != null && user.getRemoteId().equals(proxyId)) {
						long executed = this.plugin.getPrizesManager().getOrders().values().stream().filter(o -> o.examine(user, order, token)).count();
						this.plugin.getTaskManager().runTaskAsynchronously(() -> this.logTokenPrize(String.format("Handled token %s (%s) for %s(%s). Result: %d global, %d locale orders", token, order, user.getName(), String.valueOf(user.getUniqueId()), globalOrders, executed)));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void onUsersChannel(@NotNull String channel, @NotNull Player player, byte[] message) {
		if (!Portfel.CHANNEL_USERS.equals(channel)) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF(); // subchannel
		
		this.plugin.debug("PluginMessage(user|%s)\\[%s\\]: %s", player.getName(), message.length, subchannel);
		
		/* User Profile */
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
					user = new BukkitOperableUser(this.plugin, uuid, username, online, deniedInTop, balance, proxyId, this.plugin.getConfiguration().getString(BukkitConfigKey.SERVER_NAME));
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
			this.sendServerId(player);
		}
		
		/* Top Update */
		if ("Top".equals(subchannel)) {
			UUID proxyId = UUID.fromString(in.readUTF()); // proxyId
			if (this.plugin.getIdentifierManager().isValid(proxyId)) {
				int size = in.readInt(); // top size
				List<TopEntry> list = new ArrayList<>();
				for (int i = 0; i < size; i++) {
					UUID uuid = UUID.fromString(in.readUTF()); // UUID
					String name = in.readUTF(); // Name
					long balance = in.readLong();
					list.add(new TopEntry(uuid, name, balance));
				}
				
				CompletableFuture<List<TopEntry>> future = this.waitingTopUpdates.get(proxyId);
				if (future != null) {
					future.complete(list);
				}
			}
		}
		
		if ("LightTop".equals(subchannel)) {
			UUID proxyId = new UUID(in.readLong(), in.readLong()); // proxyId
			if (this.plugin.getIdentifierManager().isValid(proxyId)) {
				int size = in.readInt(); // top size
				List<TopEntry> list = new ArrayList<>();
				for (int i = 0; i < size; i++) {
					UUID uuid = new UUID(in.readLong(), in.readLong()); // UUID
					String name = IntStream.range(0, 16).mapToObj(j -> (char)(((int)in.readByte()) + 128)).filter(ch -> !ch.equals(' ')).map(String::valueOf).collect(Collectors.joining()); // Name
					long balance = in.readLong();
					list.add(new TopEntry(uuid, name, balance));
				}
				
				CompletableFuture<List<TopEntry>> future = this.waitingTopUpdates.get(proxyId);
				if (future != null) {
					future.complete(list);
				}
			}
		}
	}
	
	
	private void sendServerId(Player player) {
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user != null) {
			UUID serverId = this.plugin.getIdentifierManager().getComplementary(user.getRemoteId());
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ServerId");
			out.writeUTF(serverId.toString());
			out.writeUTF(this.plugin.getConfiguration().getString(BukkitConfigKey.SERVER_NAME));
			player.sendPluginMessage(plugin, Portfel.CHANNEL_USERS, out.toByteArray());
		}
	}
	
	
	private void sendUserRequest(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("User");
		player.sendPluginMessage(plugin, Portfel.CHANNEL_USERS, out.toByteArray());
	}
	
	
	/**
	 * Fetch actual wallet data of given player from proxy.
	 * 
	 * @param player the player
	 * @return {@link User} representation of given player
	 * @throws Exception when something went wrong
	 */
	@Override
	public @NotNull BukkitOperableUser requestPlayer(@NotNull Player player) throws Exception {
		UUID uuid = player.getUniqueId();
		CompletableFuture<BukkitOperableUser> future = this.waitingUserUpdates.get(uuid);
		if (future == null) {
			this.waitingUserUpdates.put(uuid, future = new CompletableFuture<>());
			this.sendUserRequest(player);
		}
		try {
			BukkitOperableUser user = future.get(5, TimeUnit.SECONDS);
			return user;
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			throw e;
		} finally {
			this.waitingUserUpdates.remove(uuid);
		}
	}
	
	
	/**
	 * Cancel user update task if actually pending.
	 * 
	 * @param player player to check
	 */
	@Override
	public void ensureNotUserUpdating(@NotNull Player player) {
		UUID uuid = Objects.requireNonNull(player, "player cannot be null").getUniqueId();
		CompletableFuture<BukkitOperableUser> future = this.waitingUserUpdates.get(uuid);
		if (future != null) {
			future.complete(new BukkitOperableUser(this.plugin, uuid, player.getName(), false, false, 0L, UUID.fromString("00000000-0000-0000-0000-000000000000"), this.plugin.getConfiguration().getString(BukkitConfigKey.SERVER_NAME)));
			this.waitingTopUpdates.remove(uuid);
		}
	}
	
	
	private void sendTopRequest(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("LightTop");
		player.sendPluginMessage(plugin, Portfel.CHANNEL_USERS, out.toByteArray());
	}
	
	
	/**
	 * Request top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all top entries from given proxy (miscellaneous size)
	 * @throws Exception when something went wrong
	 */
	@Override
	public @NotNull List<TopEntry> requestTop(@NotNull Player player) throws Exception {
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user == null) return new ArrayList<>();
		UUID uuid = user.getUniqueId();
		UUID proxyId = user.getRemoteId();
		CompletableFuture<List<TopEntry>> future = this.waitingTopUpdates.get(proxyId);
		if (future == null) {
			this.topUpdatesByUser.put(uuid, proxyId);
			this.waitingTopUpdates.put(proxyId, future = new CompletableFuture<>());
			this.sendTopRequest(player);
		}
		try {
			List<TopEntry> top = future.get(5, TimeUnit.SECONDS);
			return top;
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			throw e;
		} finally {
			this.waitingTopUpdates.remove(proxyId);
			this.topUpdatesByUser.remove(uuid, proxyId);
		}
	}
	
	
	/**
	 * Cancel top update task if given player is used as source for this request.
	 * 
	 * @param player player to check
	 */
	@Override
	public void ensureNotTopRequestSource(@NotNull Player player) {
		UUID proxyId = this.topUpdatesByUser.remove(Objects.requireNonNull(player, "player cannot be null").getUniqueId());
		if (proxyId != null) {
			CompletableFuture<List<TopEntry>> future = this.waitingTopUpdates.get(proxyId);
			future.complete(new ArrayList<>());
			this.waitingTopUpdates.remove(proxyId);
		}
	}
	
	
	private void sendTransaction(Player player, Transaction transaction) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Buy"); // subchannel
		out.writeUTF(this.plugin.getIdentifierManager().getComplementary(transaction.getUser().getRemoteId()).toString()); // serverId
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			dout.writeUTF(transaction.getTransactionId().toString()); // transactionId
			dout.writeUTF(this.plugin.getConfiguration().getString(BukkitConfigKey.SERVER_NAME)); // server
			dout.writeLong(transaction.getOrder().getPrice()); // value
			dout.writeUTF(this.plugin.getName()); // plugin
			dout.writeUTF(transaction.getOrder().getName()); // order
			CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getServerHashKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
		player.sendPluginMessage(plugin, Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	
	
	/**
	 * Request transaction for given player with given order.
	 * 
	 * @param player target of transaction
	 * @param order order to complete
	 * @return transaction, with completed state on success
	 */
	@Override
	public @NotNull Transaction requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order) {
		UUID transactionId = UUID.randomUUID();
		try {
			User user = this.plugin.getUserManager().getOrLoadUser(player.getUniqueId());
			if (user == null) return null;
			Transaction trans = new TransactionImpl(this.plugin, user, transactionId, order);
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
	
	
	private void logTokenPrize(@NotNull String text) {
		File f = new File(this.plugin.getDataFolder(), "token-prize.log");
		if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
		text = String.format("[%s] %s%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), text);
		try {
			Files.write(f.toPath(), Collections.singletonList(text), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
