package me.szumielxd.portfel.proxy.managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationRequestMessage;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public abstract class AccessManagerImpl<P extends PortfelProxyImpl<C>, C> implements AccessManager {
	
	
	private static final @NotNull Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private static final @NotNull String ORDERS_KEY = "orders";
	private static final @NotNull String DISPLAY_KEY = "display";
	private static final @NotNull String HASHKEY_KEY = "hashKey";
	
	
	@Getter private final P plugin;
	@Getter private final RegistrationManager registrationManager = new RegistrationManager();
	private final Path file;
	private JsonObject accessMap = null;
	
	
	protected AccessManagerImpl(@NotNull P plugin) {
		this.plugin = plugin;
		this.file = this.plugin.getDataDirectory().resolve("access.json");
	}
	
	protected abstract void preInit();
	protected abstract void postInit();
	
	/**
	 * Initialize this manager.
	 * 
	 * @return this object
	 */
	public final AccessManagerImpl<P, C> init() {
		preInit();
		if (!Files.exists(this.file)) {
			save();
			return this;
		}
		try (var br = Files.newBufferedReader(this.file)) {
			this.accessMap = GSON.fromJson(br, JsonObject.class);
		} catch (Exception e) {
			plugin.logger().severe(e, "Couldn't load access.json");
			moveBrokenFile();
		}
		postInit();
		return this;
	}
	
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
		JsonArray orders = server.get(ORDERS_KEY).getAsJsonArray();
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
				.filter(e -> serverName.equalsIgnoreCase(e.getValue().getAsJsonObject().get(DISPLAY_KEY).getAsString()))
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
		return obj.get(HASHKEY_KEY).getAsString();
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
		server.addProperty(DISPLAY_KEY, serverName.toLowerCase());
		server.add(ORDERS_KEY, new JsonArray());
		server.addProperty(HASHKEY_KEY, hashKey);
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
		JsonArray orders = server.get(ORDERS_KEY).getAsJsonArray();
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
		JsonArray orders = server.get(ORDERS_KEY).getAsJsonArray();
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
			return StreamSupport.stream(obj.getAsJsonArray(ORDERS_KEY).spliterator(), false)
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
						e -> e.getValue().getAsJsonObject().get(DISPLAY_KEY).getAsString()));
	}
	
	/**
	 * Get display name of given server.
	 * 
	 * @return string display name
	 */
	@Override
	@Contract("null -> null")
	public final @Nullable String getServerName(@NotNull UUID serverId) {
		if (this.canAccess(serverId)) {
			JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
			return obj.get(DISPLAY_KEY).getAsString();
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
		} catch (IOException | JsonIOException e) {
			plugin.logger().severe(e, "Couldn't save access.json file");
		}
	}
	
	private void moveBrokenFile() {
		var newName = file.getParent().resolve(file.getFileName() + ".broken");
		for (int i = 1; Files.exists(newName); i++) {
			newName = file.getParent().resolve(file.getFileName() + ".broken." + i);
		}
		try {
			Files.move(file, newName);
		} catch (IOException e) {
			plugin.logger().severe(e, "Couldn't move `%s` to `%s`", file.getFileName(), newName.getFileName());
		}
	}
	
	private void validateAccessMap() {
		if (this.accessMap == null) {
			throw new IllegalStateException("AccessManager is not initialized");
		}
	}
	
	private @NotNull String getCryptoKey(@NotNull UUID serverId) {
		return Objects.requireNonNull(this.plugin.getAccessManager().getHashKey(serverId), "Invalid server");
	}
	
	
	public class RegistrationManager {
		
		private final Map<UUID, RegistrationHolder> registerRequests = new HashMap<>();
		
		
		public @NotNull Optional<RegistrationHolder> getHolder(@NotNull UUID operationId) {
			return Optional.ofNullable(registerRequests.get(operationId));
		}
		
		
		public final void requestRegistration(@NotNull ProxyPlayer<C> player, @NotNull String serverName, @NotNull String hashKey) {
			player.getServer().ifPresent(server -> {
				var holder = new RegistrationHolder(UUID.randomUUID(), UUID.randomUUID(), serverName, player, hashKey);
				registerRequests.put(holder.operationId, holder);
				server.sendPluginMessage(Portfel.CHANNEL_SETUP, holder.buildRequestPacket().toBytePacket());
			});
		}
		
		
		public class RegistrationHolder {
			
			@Getter private final UUID operationId;
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
				this.task = getPlugin().getTaskManager().runTaskLater(() -> {
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
			
			public boolean tryRegister(@NotNull UUID serverId) {
				return getServerId().equals(serverId)
						&& register(getServerId(), getServerFriendyName(), getHashKey());
			}
			
			public @NotNull RegistrationRequestMessage buildRequestPacket() {
				return new RegistrationRequestMessage(
						operationId,
						new EncryptedObject<>(
								RegistrationRequestMessage.CryptoPayload.class,
								new RegistrationRequestMessage.CryptoPayload(getPlugin().getProxyId(), serverId),
								hashKey));
			}
			
		}
		
	}
	
	
	
	

}
