package me.szumielxd.portfel.bungee.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map.Entry;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import me.szumielxd.portfel.bungee.PortfelBungee;

public class AccessManager {
	
	
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
	

}
