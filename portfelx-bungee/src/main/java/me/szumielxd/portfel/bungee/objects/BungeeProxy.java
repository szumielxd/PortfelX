package me.szumielxd.portfel.bungee.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.CommonProxy;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyScheduler;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import me.szumielxd.portfel.proxy.api.objects.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeProxy implements CommonProxy {


	private final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull BungeeScheduler scheduler;
	
	
	public BungeeProxy(@NotNull PortfelBungeeImpl plugin) {
		this.plugin = plugin;
		this.scheduler = new BungeeScheduler(plugin);
	}
	

	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(this.plugin.asPlugin().getProxy().getPlayer(uuid)).map(p -> new BungeePlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull String name) {
		return Optional.ofNullable(this.plugin.asPlugin().getProxy().getPlayer(name)).map(p -> new BungeePlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @NotNull Collection<ProxyPlayer> getPlayers() {
		return this.plugin.asPlugin().getProxy().getPlayers().parallelStream().map(p -> new BungeePlayer(this.plugin, p)).collect(Collectors.toList());
	}

	@Override
	public @NotNull Optional<Collection<ProxyPlayer>> getPlayers(@NotNull String serverName) {
		return Optional.ofNullable(this.plugin.asPlugin().getProxy().getServerInfo(Objects.requireNonNull(serverName, "serverName cannot be null")))
				.map(ServerInfo::getPlayers).map(players -> players.parallelStream().map(p -> new BungeePlayer(this.plugin, p)).collect(Collectors.toList()));
	}
	
	@Override
	public @NotNull Map<String, ProxyServer> getServers() {
		return this.plugin.asPlugin().getProxy().getServers().values().parallelStream().collect(Collectors.toMap(s -> s.getName(), s -> new BungeeServer(this.plugin, s)));
	}
	
	@Override
	public @NotNull Optional<ProxyServer> getServer(@NotNull String serverName) {
		return Optional.ofNullable(this.plugin.asPlugin().getProxy().getServerInfo(serverName)).map(s -> new BungeeServer(this.plugin, s));
	}
	
	@Override
	public @NotNull ProxySender getConsole() {
		return BungeeSender.wrap(this.plugin, this.plugin.asPlugin().getProxy().getConsole());
	}
	
	@Override
	public @NotNull ProxyScheduler getScheduler() {
		return this.scheduler;
	}
	

}
