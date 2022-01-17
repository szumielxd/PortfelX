package me.szumielxd.portfel.proxy.api.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonProxy {
	
	
	public @Nullable ProxyPlayer getPlayer(@NotNull UUID uuid);
	
	public @Nullable ProxyPlayer getPlayer(@NotNull String name);
	
	public @NotNull Collection<ProxyPlayer> getPlayers();
	
	public @NotNull Optional<Collection<ProxyPlayer>> getPlayers(@NotNull String serverName);
	
	public @NotNull Map<String, ProxyServer> getServers();
	
	public @NotNull Optional<ProxyServer> getServer(@NotNull String serverName);
	
	public @NotNull ProxySender getConsole();
	
	public @NotNull ProxyScheduler getScheduler();
	

}
