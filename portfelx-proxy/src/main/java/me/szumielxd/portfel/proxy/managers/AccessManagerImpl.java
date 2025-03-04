package me.szumielxd.portfel.proxy.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.objects.PluginMessageTarget;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public abstract class AccessManagerImpl<P extends PortfelProxyImpl<C>, C> implements AccessManager {
	
	
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	
	@Getter private final P plugin;
	private final Path file;
	private JsonObject accessMap = null;
	
	
	protected AccessManagerImpl(@NotNull P plugin) {
		this.plugin = plugin;
		this.file = this.plugin.getDataDirectory().resolve("access.json");
	}
	
	/**
	 * Initialize this manager.
	 * 
	 * @return this object
	 */
	public final AccessManagerImpl<P, C> init() {
		this.preInit();
		try {
			if (!Files.exists(this.file.getParent())) {
				Files.createDirectories(this.file.getParent());
			}
			if (!Files.exists(this.file)) {
				save();
				return this;
			}
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			try {
				Files.move(file, file.getParent().resolve(this.file.getFileName() + ".broken"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		try (var br = Files.newBufferedReader(this.file)) {
			this.accessMap = GSON.fromJson(br, JsonObject.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.postInit();
		return this;
	}
	
	protected abstract void preInit();
	protected abstract void postInit();
	
	/**
	 * Check if server is registered.
	 * 
	 * @param serverId identifier of server
	 * @return true if server is registered, otherwise false
	 */
	@Override
	public final boolean canAccess(@NotNull UUID serverId) {
		validateAccessMap();
		return this.accessMap.has(serverId.toString());
	}
	
	/**
	 * Check if server is registered and can trigger given order.
	 * 
	 * @param serverId identifier of server
	 * @param order name of order (case-insensitive)
	 * @return true if server can access this order, otherwise false
	 */
	@Override
	public final boolean canAccess(@NotNull UUID serverId, @NotNull String order) {
		if (!canAccess(serverId)) {
			return false;
		}
		JsonObject server = this.accessMap.getAsJsonObject(serverId.toString());
		JsonArray orders = server.get("orders").getAsJsonArray();
		return orders.contains(new JsonPrimitive(order.toLowerCase()));
	}
	
	/**
	 * Get serverId by short-name.
	 * 
	 * @param serverName name of server used to register server (case-insensitive)
	 * @return {@link UUID} of server if given serverName is registered, otherwise null
	 */
	@Override
	public final @Nullable UUID getServerByName(@NotNull String serverName) {
		validateAccessMap();
		return this.accessMap.entrySet().stream()
				.filter(e -> serverName.equalsIgnoreCase(e.getValue().getAsJsonObject().get("display").getAsString()))
				.map(Entry::getKey).map(UUID::fromString).findAny().orElse(null);
	}
	
	/**
	 * Get hash key for given server
	 * 
	 * @param serverId server's identifier
	 * @return hash key string
	 */
	@Override
	public final @Nullable String getHashKey(@NotNull UUID serverId) {
		if (!this.canAccess(serverId)) {
			return null;
		}
		JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
		return obj.get("hashKey").getAsString();
	}
	
	/**
	 * Register new server.
	 * 
	 * @param serverId identifier of server
	 * @param serverName user-friendly text representation (case-insensitive)
	 * @param hashKey key used to hash plugin messages
	 * @return false if server is already registered, otherwise true
	 */
	@Override
	public final boolean register(@NotNull UUID serverId, @NotNull String serverName, @NotNull String hashKey) {
		if (this.canAccess(serverId)) {
			return false;
		}
		JsonObject server = new JsonObject();
		server.addProperty("display", serverName.toLowerCase());
		server.add("orders", new JsonArray());
		server.addProperty("hashKey", hashKey);
		this.accessMap.add(serverId.toString(), server);
		this.save();
		return true;
	}
	
	/**
	 * Unregister server.
	 * 
	 * @param serverId identifier of server
	 * @return false if server is not registered already, otherwise true
	 */
	@Override
	public final boolean unregister(@NotNull UUID serverId) {
		if (!canAccess(serverId)) {
			return false;
		}
		this.accessMap.remove(serverId.toString());
		this.save();
		return true;
	}
	
	/**
	 * Add new orderId to list of allowed global orders.
	 * 
	 * @param serverId ID of targeted server
	 * @param order name of the order (case-insensitive)
	 * @return false if serverID doesn't exist, or orderID is already added to allowed orders list
	 */
	@Override
	public final boolean giveAccess(@NotNull UUID serverId, @NotNull String order) {
		if (!canAccess(serverId)) {
			return false;
		}
		JsonObject server = this.accessMap.getAsJsonObject(serverId.toString());
		JsonArray orders = server.get("orders").getAsJsonArray();
		JsonPrimitive val = new JsonPrimitive(order.toLowerCase());
		if (orders.contains(val)) {
			return false;
		}
		orders.add(val);
		this.save();
		return true;
	}
	
	/**
	 * Remove orderId from list of allowed global orders.
	 * 
	 * @param serverId ID of targeted server
	 * @param order name of the order (case-insensitive)
	 * @return false if serverID doesn't exist, or orderID is not in allowed orders list
	 */
	@Override
	public final boolean takeAccess(@NotNull UUID serverId, @NotNull String order) {
		if (!canAccess(serverId)) {
			return false;
		}
		JsonObject server = this.accessMap.getAsJsonObject(serverId.toString());
		JsonArray orders = server.get("orders").getAsJsonArray();
		JsonPrimitive val = new JsonPrimitive(order.toLowerCase());
		if (!orders.contains(val)) return false;
		orders.remove(val);
		this.save();
		return true;
	}
	
	/**
	 * Get all global orders allowed for given server.
	 * 
	 * @param serverId server to check
	 * @return list of allowed order's names
	 */
	@Override
	public final @Nullable List<String> getAllowedOrders(@NotNull UUID serverId) {
		if (this.canAccess(serverId)) {
			JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
			return StreamSupport.stream(obj.getAsJsonArray("orders").spliterator(), false)
					.map(JsonElement::getAsString)
					.toList();
		}
		return null;
	}
	
	/**
	 * Get names of all registered servers accessed by server's ID.
	 * 
	 * @return map of server names and IDs
	 */
	@Override
	public final @NotNull Map<UUID, String> getServerNames() {
		return this.accessMap.entrySet().stream()
				.collect(Collectors.toMap(
						e -> UUID.fromString(e.getKey()),
						e -> e.getValue().getAsJsonObject().get("display").getAsString()));
	}
	
	/**
	 * Get display name of given server.
	 * 
	 * @return string display name
	 */
	@Override
	public final @Nullable String getServerName(@NotNull UUID serverId) {
		if (this.canAccess(serverId)) {
			JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
			return obj.get("display").getAsString();
		}
		return null;
	}
	
	/**
	 * Save servers list.
	 */
	private final void save() {
		try {
			if (!Files.exists(this.file.getParent())) {
				Files.createDirectories(this.file.getParent());
			}
			try (var bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
				GSON.toJson(accessMap, bw);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public final void pendingRegistration(ProxyPlayer<C> player, String serverName, String hashKey) {
		if (player != null) {
			Optional<ProxyServerConnection<C>> srv = player.getServer();
			if (srv.isPresent()) {
				UUID serverId = UUID.randomUUID();
				UUID operationId = UUID.randomUUID();
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Register"); // subchannel
				try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
						DataOutputStream dout = new DataOutputStream(bout);) {
					dout.writeUTF(operationId.toString()); // operation ID
					dout.writeUTF(this.plugin.getProxyId().toString()); // proxy ID
					dout.writeUTF(serverId.toString()); // server ID
					CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), hashKey);
				} catch (IOException e) {
					e.printStackTrace();
				}
				srv.get().sendPluginMessage(Portfel.CHANNEL_SETUP, out.toByteArray());
				this.registerRequests.put(operationId, new RegistrationHolder(operationId, serverId, serverName, player, hashKey));
			}
		}
	}
	
	
	private final Map<UUID, RegistrationHolder> registerRequests = new HashMap<>();
	
	
	protected final boolean isListendChannel(@Nullable String tag) {
		return Portfel.CHANNEL_SETUP.equals(tag)
				|| Portfel.CHANNEL_TRANSACTIONS.equals(tag)
				|| Portfel.CHANNEL_USERS.equals(tag)
				|| Portfel.CHANNEL_LEGACY_BUNGEE.equals(tag)
				|| Portfel.CHANNEL_BUNGEE.equals(tag);
	}
	
	
	@SuppressWarnings("unchecked")
	protected final Optional<Boolean> onPluginMessage(@NotNull PluginMessageTarget sender, @NotNull PluginMessageTarget target, @NotNull String tag, byte[] message) {
		if (sender instanceof ProxyServerConnection server && target instanceof ProxyPlayer) {
			ProxyPlayer<C> player = (ProxyPlayer<C>) target;
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			
			if (Portfel.CHANNEL_SETUP.equals(tag)) {
				if ("Register".equals(subchannel)) return this.onRegistrationCallback(server, player, tag, subchannel, in);
				if ("Validate".equals(subchannel)) return this.onNonBungeeRegistrationValidCheck(server, player, tag, subchannel, in);
			}
			if (Portfel.CHANNEL_BUNGEE.equals(tag) || Portfel.CHANNEL_LEGACY_BUNGEE.equals(tag)) {
				if ("ForwardToPlayer".equals(subchannel)) return this.onRegistrationValidCheck(server, player, tag, subchannel, in);
			}
			
		}
		return Optional.empty();
	}
	
	
	
	
	
	// BungeeCord
	// ForwardToPlayer
	private Optional<Boolean> onRegistrationValidCheck(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		this.plugin.debug("[%s] onRegistrationValidCheck", "AccessManagerImpl");
		in.readUTF(); // username
		String channel = in.readUTF(); // custom channel
		if (Portfel.CHANNEL_SETUP.equals(channel)) {
			byte[] bytes = new byte[in.readShort()];
			in.readFully(bytes);
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
			try {
				String ch = is.readUTF(); // 
				if ("Validate".equals(ch)) { // subchannel...
					UUID uuid = UUID.fromString(is.readUTF()); // actionId to validate
					
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF(subchannel);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream os = new DataOutputStream(baos);
					os.writeUTF("Validate");
					os.writeUTF(uuid.toString()); // validated actionId
					os.writeBoolean(this.registerRequests.containsKey(uuid)); // validity result
					out.writeShort(baos.toByteArray().length);
					out.write(baos.toByteArray());
					sender.sendPluginMessage(tag, out.toByteArray());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return Optional.of(true);
		}
		return Optional.empty();
	}
	
	
	private Optional<Boolean> onNonBungeeRegistrationValidCheck(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		this.plugin.debug("[%s] onNonBungeeRegistrationValidCheck", "AccessManagerImpl");
		UUID uuid = UUID.fromString(in.readUTF()); // actionId to validate
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ForwardToPlayer");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos); 
		try {
			os.writeUTF("Validate");
			os.writeUTF(uuid.toString());
			os.writeBoolean(this.registerRequests.containsKey(uuid)); // validity result
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// validated actionId
		out.writeShort(baos.toByteArray().length);
		out.write(baos.toByteArray());
		sender.sendPluginMessage(Portfel.CHANNEL_BUNGEE, out.toByteArray());
		return Optional.of(true);
	}
	
	
	// Setup
	// Register
	private Optional<Boolean> onRegistrationCallback(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		this.plugin.debug("[%s] onRegistrationCallback", "AccessManagerImpl");
		UUID operationId = null;
		try {
			operationId = UUID.fromString(in.readUTF());
		} catch (IllegalArgumentException e) {}
		if (operationId != null) {
			RegistrationHolder holder = this.registerRequests.get(operationId);
			if (holder != null) {
				holder.done();
				byte[] data;
				try {
					data = CryptoUtils.decodeBytesFromInput(in, holder.getHashKey());
				} catch (IllegalArgumentException e) {
					// ignore malformed messages
					return Optional.of(true);
				}
				try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(data))) {
					String status = din.readUTF();
					if ("Ok".equals(status)) {
						UUID proxyId = UUID.fromString(din.readUTF());
						UUID serverId = UUID.fromString(din.readUTF());
						if (this.plugin.getProxyId().equals(proxyId) && holder.getServerId().equals(serverId) && this.register(holder.getServerId(), holder.getServerFriendyName(), holder.getHashKey())) {
							ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_SUCCESS.draft(
									ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(
											holder.getServerFriendyName(),
											ProxyLangKey.COMMAND_VALUENAMES_SERVERFRIENDLYNAME),
									ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(
											holder.getServerId().toString(), 
											ProxyLangKey.COMMAND_VALUENAMES_SERVERID))
									.sendPrefixed(target);
							return Optional.of(true);
						}
					} else if ("Set".equals(status)) {
						UUID proxyId = UUID.fromString(din.readUTF());
						UUID serverId = UUID.fromString(din.readUTF());
						if (this.plugin.getProxyId().equals(proxyId) && this.canAccess(serverId)) {
							ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_ALREADY
									.draft(ProxyLangKey.MAIN_MESSAGE_INSERTION
											.draft(serverId.toString(), 
													ProxyLangKey.COMMAND_VALUENAMES_SERVERID))
									.sendPrefixed(target);
							return Optional.of(true);
						}
					}
					ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_ERROR
							.draft()
							.sendPrefixed(target);
					return Optional.of(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Optional.of(true);
	}
	
	private void validateAccessMap() {
		if (this.accessMap == null) {
			throw new IllegalStateException("AccessManager is not initialized");
		}
	}
	
	
	private class RegistrationHolder {
		
		private final UUID operationId;
		@Getter private final UUID serverId;
		@Getter private final String serverFriendyName;
		@Getter private final CommonPlayer<C> sender;
		@Getter private final String hashKey;
		private final ExecutedTask task;
		
		public RegistrationHolder(@NotNull UUID operationId, @NotNull UUID serverId, @NotNull String serverName, @NotNull CommonPlayer<C> sender, @NotNull String hashKey) {
			this.operationId = operationId;
			this.serverId = serverId;
			this.serverFriendyName = serverName;
			this.sender = sender;
			this.hashKey = hashKey;
			this.task = plugin.getTaskManager().runTaskLater(() -> {
				this.done();
				ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT
						.draft()
						.sendPrefixed(sender);
			}, 1, TimeUnit.SECONDS);
		}
		
		public void done() {
			this.task.cancel();
			registerRequests.remove(this.operationId);
		}
		
	}
	

}
