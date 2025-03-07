package me.szumielxd.portfel.bukkit.objects;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;

public class BukkitImaginaryUser extends BukkitOperableUser {

	public BukkitImaginaryUser(@NotNull PortfelBukkitImpl plugin, @NotNull UUID uuid) {
		super(plugin, uuid, "", false, 0, 0, UUID.randomUUID(), "");
	}

}
