package me.szumielxd.portfel.proxy.api.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonServer;

public interface CommonProxy extends CommonServer {
	
	
	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull UUID uuid);
	
	@Override
	public @Nullable ProxyPlayer getPlayer(@NotNull String name);
	
	@Override
	public @NotNull Collection<ProxyPlayer> getPlayers();
	
	public @NotNull Optional<Collection<ProxyPlayer>> getPlayers(@NotNull String serverName);
	
	public @NotNull Map<String, ProxyServer> getServers();
	
	public @NotNull Optional<ProxyServer> getServer(@NotNull String serverName);
	
	@Override
	public @NotNull ProxySender getConsole();
	
	public @NotNull ProxyScheduler getScheduler();
	

}
