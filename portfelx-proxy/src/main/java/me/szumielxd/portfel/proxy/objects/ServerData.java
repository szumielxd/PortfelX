package me.szumielxd.portfel.proxy.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;

public class ServerData<C> {
	
	
	private final @NotNull String serverName;
	private final @NotNull Collection<ProxyPlayer<C>> players;
	
	
	public ServerData(@NotNull String serverName, @NotNull Collection<ProxyPlayer<C>> players) {
		this.serverName = Objects.requireNonNull(serverName, "serverName cannot be null");
		this.players = Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(players)), "players cannot be null");
	}
	
	
	public @NotNull String getName() {
		return this.serverName;
	}
	
	
	public @NotNull Collection<ProxyPlayer<C>> getPlayers() {
		return this.players;
	}
	

}
