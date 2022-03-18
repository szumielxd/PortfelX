package me.szumielxd.portfel.velocity.objects;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;

public class VelocityServerConnection implements ProxyServerConnection {
	
	
	private final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull ServerConnection server;
	
	
	public VelocityServerConnection(@NotNull PortfelVelocityImpl plugin, @NotNull ServerConnection server) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.server = Objects.requireNonNull(server, "server cannot be null");
	}
	

	@Override
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message) {
		this.server.sendPluginMessage(tag.indexOf(':') >= 0 ? MinecraftChannelIdentifier.from(tag) : new LegacyChannelIdentifier(tag), message);
	}

	@Override
	public @NotNull ProxyServer getServer() {
		return new VelocityServer(this.plugin, this.server.getServer());
	}
	
	@Override
	public int hashCode() {
		return this.server.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof VelocityServerConnection)) return false;
		return this.server.equals(((VelocityServerConnection)obj).server);
	}

}
