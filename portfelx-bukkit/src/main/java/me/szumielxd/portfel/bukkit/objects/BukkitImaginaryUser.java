package me.szumielxd.portfel.bukkit.objects;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;

public class BukkitImaginaryUser extends BukkitOperableUser {

	public BukkitImaginaryUser(@NotNull PortfelBukkitImpl plugin, @NotNull UUID uuid) {
		super(plugin, uuid, "", Optional.ofNullable(Bukkit.getPlayer(uuid)).filter(Player::isOnline).isPresent(), false, 0, 0, UUID.randomUUID(), "");
	}

}
