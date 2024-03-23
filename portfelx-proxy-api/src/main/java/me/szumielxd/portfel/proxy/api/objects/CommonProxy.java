package me.szumielxd.portfel.proxy.api.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonServer;

public interface CommonProxy<C> extends CommonServer<C> {
	
	
	@Override
	public @Nullable ProxyPlayer<C> getPlayer(@NotNull UUID uuid);
	
	@Override
	public @Nullable ProxyPlayer<C> getPlayer(@NotNull String name);
	
	@Override
	public @NotNull Collection<? extends ProxyPlayer<C>> getPlayers();
	
	public @NotNull Optional<Collection<? extends ProxyPlayer<C>>> getPlayers(@NotNull String serverName);
	
	public @NotNull Map<String, ? extends ProxyServer<C>> getServers();
	
	public @NotNull Optional<ProxyServer<C>> getServer(@NotNull String serverName);
	
	@Override
	public @NotNull ProxySender<C> getConsole();
	
	public @NotNull ProxyScheduler getScheduler();
	

}
