package me.szumielxd.portfel.velocity.objects;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;

public class VelocityServer implements ProxyServer {
	
	
	private final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull RegisteredServer server;
	
	
	public VelocityServer(@NotNull PortfelVelocityImpl plugin, @NotNull RegisteredServer server) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.server = Objects.requireNonNull(server, "server cannot be null");
	}
	

	@Override
	public @NotNull Collection<ProxyPlayer> getPlayers() {
		return this.server.getPlayersConnected().parallelStream().map(p -> new VelocityPlayer(this.plugin, p)).collect(Collectors.toList());
	}

	@Override
	public @NotNull String getName() {
		return this.server.getServerInfo().getName();
	}
	
	@Override
	public @NotNull SocketAddress getAddress() {
		return this.server.getServerInfo().getAddress();
	}
	
	@Override
	public boolean isRestricted() {
		return false;
	}
	
	@Override
	public @NotNull String getPermission() {
		return "";
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
