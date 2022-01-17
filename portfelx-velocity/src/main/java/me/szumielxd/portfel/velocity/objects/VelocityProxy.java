package me.szumielxd.portfel.velocity.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.szumielxd.portfel.proxy.api.objects.CommonProxy;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyScheduler;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;

public class VelocityProxy implements CommonProxy {
	
	
	private final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull VelocityScheduler scheduler;
	
	
	public VelocityProxy(@NotNull PortfelVelocityImpl plugin) {
		this.plugin = plugin;
		this.scheduler = new VelocityScheduler(plugin);
	}
	

	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull UUID uuid) {
		return this.plugin.getProxy().getPlayer(uuid).map(p -> new VelocityPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull String name) {
		return this.plugin.getProxy().getPlayer(name).map(p -> new VelocityPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @NotNull Collection<ProxyPlayer> getPlayers() {
		return this.plugin.getProxy().getAllPlayers().parallelStream().map(p -> new VelocityPlayer(this.plugin, p)).collect(Collectors.toList());
	}

	@Override
	public @NotNull Optional<Collection<ProxyPlayer>> getPlayers(@NotNull String serverName) {
		return this.plugin.getProxy().getServer(Objects.requireNonNull(serverName, "serverName cannot be null"))
				.map(RegisteredServer::getPlayersConnected)
				.map(list -> list.parallelStream().map(p -> new VelocityPlayer(this.plugin, p)).collect(Collectors.toList()));
	}
	
	@Override
	public @NotNull Map<String, ProxyServer> getServers() {
		return this.plugin.getProxy().getAllServers().parallelStream().collect(Collectors.toMap(s -> s.getServerInfo().getName(), s -> new VelocityServer(this.plugin, s)));
	}
	
	@Override
	public @NotNull Optional<ProxyServer> getServer(@NotNull String serverName) {
		return this.plugin.getProxy().getServer(serverName).map(s -> new VelocityServer(this.plugin, s));
	}
	
	@Override
	public @NotNull ProxySender getConsole() {
		return VelocitySender.wrap(this.plugin, this.plugin.getProxy().getConsoleCommandSource());
	}
	
	@Override
	public @NotNull ProxyScheduler getScheduler() {
		return this.scheduler;
	}
	

}
