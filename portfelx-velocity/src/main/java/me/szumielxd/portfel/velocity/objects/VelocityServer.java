package me.szumielxd.portfel.velocity.objects;

import java.net.SocketAddress;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import lombok.AllArgsConstructor;
import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class VelocityServer implements ProxyServer<Component> {
	
	
	private final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull RegisteredServer server;
	

	@Override
	public @NotNull Collection<VelocityPlayer> getPlayers() {
		return this.server.getPlayersConnected().parallelStream()
				.map(p -> new VelocityPlayer(this.plugin, p))
				.toList();
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
