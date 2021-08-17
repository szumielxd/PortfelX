package me.szumielxd.portfel.bukkit.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.IdentifierManager;

public class IdentifierManagerImpl implements IdentifierManager {
	
	
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	
	private final PortfelBukkitImpl plugin;
	private final File file;
	private JsonObject accessMap = null;
	
	
	public IdentifierManagerImpl(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "access.json");
	}
	
	
	/**
	 * Initialize IdentifierManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public IdentifierManagerImpl init() {
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (file.exists()) {
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
	 * Check whether given proxy Id is already registered on server
	 * 
	 * @param proxyId UUID of proxy
	 * @return true if proxy is registered, otherwise false
	 */
	@Override
	public boolean isValid(@NotNull UUID proxyId) {
		return this.accessMap.has(proxyId.toString());
	}
	
	/**
	 * Get complementary server ID for given proxy Id
	 * 
	 * @param proxyId UUID of proxy
	 * @return server ID, or null
	 */
	@Override
	public @Nullable UUID getComplementary(@NotNull UUID proxyId) {
		if (!this.isValid(proxyId)) return null;
		return UUID.fromString(this.accessMap.get(proxyId.toString()).getAsString());
	}
	
	/**
	 * Register new proxy-server pair
	 * 
	 * @param proxyId UUID of proxy
	 * @param serverId UUID of server
	 * @return true if pair was successfully registered, otherwise false
	 */
	@Override
	public boolean register(UUID proxyId, UUID serverId) {
		if (this.isValid(proxyId)) return false;
		this.accessMap.addProperty(proxyId.toString(), serverId.toString());
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
