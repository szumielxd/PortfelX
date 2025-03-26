package me.szumielxd.portfel.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

public interface AbstractPortfelGui {
	
	
	public @NotNull MessageDraft getTitle(@NotNull User user, @NotNull Player player);
	
	public int getSize();
	
	public void onClick(@NotNull Player player, int slot);
	
	public void setup(@NotNull Player player, @NotNull Inventory inventory);
	

}
