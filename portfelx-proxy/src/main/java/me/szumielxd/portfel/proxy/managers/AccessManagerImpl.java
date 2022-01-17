package me.szumielxd.portfel.proxy.managers;

import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.objects.PluginMessageTarget;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public abstract class AccessManagerImpl implements AccessManager {
	
	
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	
	private final PortfelProxyImpl plugin;
	private final File file;
	private JsonObject accessMap = null;
	
	
	public AccessManagerImpl(@NotNull PortfelProxyImpl plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "access.json");
	}
	
	/**
	 * Initialize this manager.
	 * 
	 * @return this object
	 */
	public final AccessManagerImpl init() {
		this.preInit();
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) {
			try {
				file.createNewFile();
				Files.write(file.toPath(), GSON.toJson(this.accessMap = new JsonObject()).getBytes(StandardCharsets.UTF_8));
				return this;
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				File to = new File(this.plugin.getDataFolder(), file.getName() + ".broken");
				file.renameTo(to);
				e.printStackTrace();
			}
		}
		try {
			this.accessMap = GSON.fromJson(new FileReader(file), JsonObject.class);
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		return this.accessMap.has(serverId.toString());
	}
	
	/**
	 * Check if server can trigger given order.
	 * 
	 * @param serverId identifier of server
	 * @param order name of order (case-insensitive)
	 * @return true if server can access this order, otherwise false
	 */
	@Override
	public final boolean canAccess(@NotNull UUID serverId, String order) {
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (!this.accessMap.has(serverId.toString())) return false;
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
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
		if (!this.canAccess(serverId)) return null;
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (this.accessMap.has(serverId.toString())) return false;
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (!this.accessMap.has(serverId.toString())) return false;
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (!this.accessMap.has(serverId.toString())) return false;
		JsonObject server = this.accessMap.getAsJsonObject(serverId.toString());
		JsonArray orders = server.get("orders").getAsJsonArray();
		JsonPrimitive val = new JsonPrimitive(order.toLowerCase());
		if (orders.contains(val)) return false;
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
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (!this.accessMap.has(serverId.toString())) return false;
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
		if (!this.canAccess(serverId)) return null;
		JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
		return StreamSupport.stream(obj.getAsJsonArray("orders").spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toList());
	}
	
	/**
	 * Get names of all registered servers accessed by server's ID.
	 * 
	 * @return map of server names and IDs
	 */
	@Override
	public final Map<UUID, String> getServerNames() {
		Map<UUID, String> serverNames = new HashMap<>();
		this.accessMap.entrySet().forEach(e -> serverNames.put(UUID.fromString(e.getKey()), e.getValue().getAsJsonObject().get("display").getAsString()));
		return serverNames;
	}
	
	/**
	 * Save servers list.
	 */
	private final void save() {
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try {
			Files.write(file.toPath(), GSON.toJson(this.accessMap).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public final void pendingRegistration(ProxyPlayer player, String serverName, String hashKey) {
		if (player != null) {
			Optional<ProxyServerConnection> srv = player.getServer();
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
	
	
	protected PortfelProxyImpl getPlugin() {
		return this.plugin;
	}
	
	
	protected final Optional<Boolean> onPluginMessage(@NotNull PluginMessageTarget sender, @NotNull PluginMessageTarget target, @NotNull String tag, byte[] message) {
		if (sender instanceof ProxyServerConnection && target instanceof ProxyPlayer) {
			ProxyServerConnection server = (ProxyServerConnection) sender;
			ProxyPlayer player = (ProxyPlayer) target;
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			
			if (Portfel.CHANNEL_SETUP.equals(tag)) {
				if ("Register".equals(subchannel)) return this.onRegistrationCallback(server, player, tag, subchannel, in);
			}
			if (Portfel.CHANNEL_BUNGEE.equals(tag) || Portfel.CHANNEL_LEGACY_BUNGEE.equals(tag)) {
				if ("ForwardToPlayer".equals(subchannel)) return this.onRegistrationValidCheck(server, player, tag, subchannel, in);
			}
			
		}
		return Optional.empty();
	}
	
	
	
	
	
	// BungeeCord
	// ForwardToPlayer
	private Optional<Boolean> onRegistrationValidCheck(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
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
	
	
	// Setup
	// Register
	private Optional<Boolean> onRegistrationCallback(@NotNull ProxyServerConnection sender, @NotNull ProxyPlayer target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
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
						if (this.plugin.getProxyId().equals(proxyId)) {
							if (holder.getServerId().equals(serverId)) {
								if (this.register(holder.getServerId(), holder.getServerFriendyName(), holder.getHashKey())) {
									String srvId = holder.getServerId().toString();
									String srvName = holder.getServerFriendyName();
									Component srvIdComp = Component.text(srvId, AQUA, UNDERLINED)
											.clickEvent(ClickEvent.suggestCommand(srvId)).insertion(srvId)
											.hoverEvent(Component.text("» ", DARK_AQUA).append(LangKey.MAIN_MESSAGE_INSERTION
													.component(AQUA, Component.text("server ID"))
											));
									Component srvNameComp = Component.text(srvName, AQUA, UNDERLINED)
											.clickEvent(ClickEvent.suggestCommand(srvName)).insertion(srvName)
											.hoverEvent(Component.text("» ", DARK_AQUA).append(LangKey.MAIN_MESSAGE_INSERTION
													.component(AQUA, Component.text("server friendly name"))
											));
									
									holder.getSender().sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_SUCCESS
											.component(LIGHT_PURPLE, srvIdComp, srvNameComp)));
									return Optional.of(true);
								}
							}
						}
					} else if ("Set".equals(status)) {
						UUID proxyId = UUID.fromString(din.readUTF());
						UUID serverId = UUID.fromString(din.readUTF());
						if (this.plugin.getProxyId().equals(proxyId)) {
							if (this.canAccess(serverId)) {
								String srvId = serverId.toString();
								Component srvIdComp = Component.text(srvId, AQUA, UNDERLINED)
										.clickEvent(ClickEvent.suggestCommand(srvId)).insertion(srvId)
										.hoverEvent(Component.text("» ", DARK_AQUA).append(LangKey.MAIN_MESSAGE_INSERTION
												.component(AQUA, Component.text("server ID"))
										));
								
								holder.getSender().sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_ALREADY
										.component(RED, srvIdComp)));
								return Optional.of(true);
							}
						}
					}
					holder.getSender().sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_ERROR.component(DARK_RED)));
					return Optional.of(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Optional.of(true);
	}
	
	
	private class RegistrationHolder {
		
		private final UUID operationId;
		private final UUID serverId;
		private final String serverName;
		private final CommonPlayer sender;
		private final String hashKey;
		private final ExecutedTask task;
		
		public RegistrationHolder(@NotNull UUID operationId, @NotNull UUID serverId, @NotNull String serverName, @NotNull CommonPlayer sender, @NotNull String hashKey) {
			this.operationId = operationId;
			this.serverId = serverId;
			this.serverName = serverName;
			this.sender = sender;
			this.hashKey = hashKey;
			this.task = plugin.getTaskManager().runTaskLater(() -> {
				this.done();
				this.sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT.component(RED)));
			}, 1, TimeUnit.SECONDS);
		}
		
		public @NotNull UUID getServerId() {
			return this.serverId;
		}
		
		public @NotNull String getServerFriendyName() {
			return this.serverName;
		}
		
		public @NotNull CommonPlayer getSender() {
			return this.sender;
		}
		
		public @NotNull String getHashKey() {
			return this.hashKey;
		}
		
		public void done() {
			this.task.cancel();
			registerRequests.remove(this.operationId);
		}
		
	}
	

}
