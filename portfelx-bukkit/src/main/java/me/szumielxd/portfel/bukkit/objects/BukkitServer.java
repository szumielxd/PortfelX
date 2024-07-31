package me.szumielxd.portfel.bukkit.objects;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonServer;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import net.kyori.adventure.text.Component;

public class BukkitServer implements CommonServer<Component> {
	
	
	private final @NotNull PortfelBukkitImpl plugin;
	
	
	public BukkitServer(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public @Nullable BukkitPlayer getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(this.plugin.getServer().getPlayer(uuid))
				.map(p -> new BukkitPlayer(this.plugin, p))
				.orElse(null);
	}

	@Override
	public @Nullable BukkitPlayer getPlayer(@NotNull String name) {
		return Optional.ofNullable(this.plugin.getServer().getPlayer(name))
				.map(p -> new BukkitPlayer(this.plugin, p))
				.orElse(null);
	}

	@Override
	public @NotNull Collection<BukkitPlayer> getPlayers() {
		return this.plugin.getServer().getOnlinePlayers()
				.parallelStream()
				.map(p -> new BukkitPlayer(this.plugin, p))
				.toList();
	}
	
	@Override
	public @NotNull BukkitSender getConsole() {
		return BukkitSender.wrap(this.plugin, this.plugin.getServer().getConsoleSender());
	}
	

}
