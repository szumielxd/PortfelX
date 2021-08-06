package me.szumielxd.portfel.bungee.managers;

import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.objects.CommonPlayer;
import me.szumielxd.portfel.common.objects.ExecutedTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class AccessManager implements Listener {
	
	
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	
	private final PortfelBungee plugin;
	private final File file;
	private JsonObject accessMap = null;
	
	
	public AccessManager(@NotNull PortfelBungee plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "access.json");
	}
	
	/**
	 * Initialize this manager.
	 * 
	 * @return this object
	 */
	public AccessManager init() {
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) {
			try {
				this.accessMap = GSON.fromJson(new FileReader(file), JsonObject.class);
				return this;
			} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
				File to = new File(this.plugin.getDataFolder(), file.getName() + ".broken");
				file.renameTo(to);
				e.printStackTrace();
			}
		}
		try {
			Files.write(file.toPath(), GSON.toJson(this.accessMap = new JsonObject()).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
		return this;
	}
	
	/**
	 * Check if server is registered.
	 * 
	 * @param serverId identifier of server
	 * @return true if server is registered, otherwise false
	 */
	public boolean canAccess(@NotNull UUID serverId) {
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
	public boolean canAccess(@NotNull UUID serverId, String order) {
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
	public @Nullable UUID getServerByName(@NotNull String serverName) {
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		return this.accessMap.entrySet().stream()
				.filter(e -> serverName.equalsIgnoreCase(e.getValue().getAsJsonObject().get("display").getAsString()))
				.map(Entry::getKey).map(UUID::fromString).findAny().orElse(null);
	}
	
	/**
	 * Register new server.
	 * 
	 * @param serverId identifier of server
	 * @param serverName user-friendly text representation (case-insensitive)
	 * @return false if server is already registered, otherwise true
	 */
	public boolean register(@NotNull UUID serverId, @NotNull String serverName) {
		if (this.accessMap == null) throw new IllegalStateException("AccessManager is not initialized");
		if (this.accessMap.has(serverId.toString())) return false;
		JsonObject server = new JsonObject();
		server.addProperty("display", serverName.toLowerCase());
		server.add("orders", new JsonArray());
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
	public boolean unregister(@NotNull UUID serverId) {
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
	public boolean giveAccess(@NotNull UUID serverId, @NotNull String order) {
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
	public boolean takeAccess(@NotNull UUID serverId, @NotNull String order) {
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
	
	public @Nullable List<String> getAllowedOrders(@NotNull UUID serverId) {
		if (!this.canAccess(serverId)) return null;
		JsonObject obj = this.accessMap.getAsJsonObject(serverId.toString());
		return StreamSupport.stream(obj.getAsJsonArray("orders").spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toList());
	}
	
	public Map<UUID, String> getServerNames() {
		Map<UUID, String> serverNames = new HashMap<>();
		this.accessMap.entrySet().forEach(e -> serverNames.put(UUID.fromString(e.getKey()), e.getValue().getAsJsonObject().get("display").getAsString()));
		return serverNames;
	}
	
	/**
	 * Save servers list.
	 */
	private void save() {
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try {
			Files.write(file.toPath(), GSON.toJson(this.accessMap).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void pendingRegistration(CommonPlayer player, String serverName) {
		ProxiedPlayer pp = this.plugin.getProxy().getPlayer(player.getUniqueId());
		if (pp != null) {
			Server srv = pp.getServer();
			if (srv != null) {
				UUID serverId = UUID.randomUUID();
				UUID operationId = UUID.randomUUID();
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Register"); // subchannel
				out.writeUTF(operationId.toString()); // operation ID
				out.writeUTF(this.plugin.getProxyId().toString()); // proxy ID
				out.writeUTF(serverId.toString()); // server ID
				srv.sendData(Portfel.CHANNEL_SETUP, out.toByteArray());
				this.registerRequests.put(operationId, new RegistrationHolder(operationId, serverId, serverName, player));
			}
		}
	}
	
	
	private Map<UUID, RegistrationHolder> registerRequests = new HashMap<>();
	
	
	@EventHandler
	private void onRegistrationCallback(PluginMessageEvent event) {
		if (Portfel.CHANNEL_SETUP.equals(event.getTag())) {
			event.setCancelled(true);
			if (event.getSender() instanceof Server) {
				ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
				String subchannel = in.readUTF();
				if ("Register".equals(subchannel)) {
					UUID operationId = null;
					try {
						operationId = UUID.fromString(in.readUTF());
					} catch (IllegalArgumentException e) {}
					if (operationId != null) {
						RegistrationHolder holder = this.registerRequests.get(operationId);
						if (holder != null) {
							holder.done();
							String status = in.readUTF();
							if ("Ok".equals(status)) {
								UUID proxyId = UUID.fromString(in.readUTF());
								UUID serverId = UUID.fromString(in.readUTF());
								if (this.plugin.getProxyId().equals(proxyId)) {
									if (holder.getServerId().equals(serverId)) {
										if (this.register(holder.getServerId(), holder.getServerFriendyName())) {
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
											return;
										}
									}
								}
							} else if ("Set".equals(status)) {
								UUID proxyId = UUID.fromString(in.readUTF());
								UUID serverId = UUID.fromString(in.readUTF());
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
										return;
									}
								}
							}
							holder.getSender().sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_ERROR.component(DARK_RED)));
							return;
						}
					}
				}
			}
		}
	}
	
	
	private class RegistrationHolder {
		
		private final UUID operationId;
		private final UUID serverId;
		private final String serverName;
		private final CommonPlayer sender;
		private final ExecutedTask task;
		
		public RegistrationHolder(@NotNull UUID operationId, @NotNull UUID serverId, @NotNull String serverName, @NotNull CommonPlayer sender) {
			this.operationId = operationId;
			this.serverId = serverId;
			this.serverName = serverName;
			this.sender = sender;
			this.task = plugin.getTaskManager().runTaskLater(() -> {
				this.done();
				this.sender.sendMessage(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT.component(RED)));
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
		
		public void done() {
			this.task.cancel();
			registerRequests.remove(this.operationId);
		}
		
	}
	

}
