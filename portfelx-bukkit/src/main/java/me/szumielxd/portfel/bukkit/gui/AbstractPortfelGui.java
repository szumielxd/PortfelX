package me.szumielxd.portfel.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.objects.User;
import net.kyori.adventure.text.Component;

public interface AbstractPortfelGui {
	
	
	public @NotNull Component getTitle(User user);
	
	public int getSize();
	
	public void onClick(@NotNull Player player, int slot);
	
	public void setup(@NotNull Player player, @NotNull Inventory inventory);
	

}
