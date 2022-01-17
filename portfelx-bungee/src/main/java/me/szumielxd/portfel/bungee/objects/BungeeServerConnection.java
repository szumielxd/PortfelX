package me.szumielxd.portfel.bungee.objects;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import net.md_5.bungee.api.connection.Server;

public class BungeeServerConnection implements ProxyServerConnection {
	
	
	private final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull Server server;
	
	
	public BungeeServerConnection(@NotNull PortfelBungeeImpl plugin, @NotNull Server server) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.server = Objects.requireNonNull(server, "server cannot be null");
	}
	

	@Override
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message) {
		this.server.sendData(tag, message);
	}

	@Override
	public @NotNull ProxyServer getServer() {
		return new BungeeServer(this.plugin, this.server.getInfo());
	}
	
	@Override
	public int hashCode() {
		return this.server.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.server.equals(obj);
	}

}
