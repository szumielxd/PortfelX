package me.szumielxd.portfel.bukkit.objects;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.CommonServer;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;

public class BukkitServer implements CommonServer {
	
	
	private final @NotNull PortfelBukkitImpl plugin;
	
	
	public BukkitServer(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(this.plugin.getServer().getPlayer(uuid)).map(p -> new BukkitPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull String name) {
		return Optional.ofNullable(this.plugin.getServer().getPlayer(name)).map(p -> new BukkitPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @NotNull Collection<CommonPlayer> getPlayers() {
		return this.plugin.getServer().getOnlinePlayers().parallelStream().map(p -> new BukkitPlayer(this.plugin, p)).collect(Collectors.toList());
	}
	
	@Override
	public @NotNull CommonSender getConsole() {
		return BukkitSender.wrap(this.plugin, this.plugin.getServer().getConsoleSender());
	}
	

}
