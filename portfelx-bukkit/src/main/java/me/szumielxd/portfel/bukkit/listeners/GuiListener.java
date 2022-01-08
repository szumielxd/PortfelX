package me.szumielxd.portfel.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import me.szumielxd.portfel.bukkit.gui.PortfelGuiHolder;

public class GuiListener implements Listener {
	
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			Inventory inv = event.getClickedInventory();
			if (inv != null) {
				if (PortfelGuiHolder.class.isInstance(inv.getHolder())) {
					event.setResult(Result.DENY);
					PortfelGuiHolder holder = (PortfelGuiHolder) inv.getHolder();
					holder.getGui().onClick(player, event.getSlot());
				} else if (event.isShiftClick() && PortfelGuiHolder.class.isInstance(event.getInventory().getHolder())) {
					event.setResult(Result.DENY);
				}
			}
		}
	}
	
	
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			InventoryView view = event.getView();
			if (event.getView().getTopInventory().getType().equals(InventoryType.CRAFTING)) return;
			if (event.getRawSlots().stream().map(s -> this.getInventory(view, s)).map(Inventory::getHolder).anyMatch(PortfelGuiHolder.class::isInstance)) event.setResult(Result.DENY);
		}
	}
	
	
    private final @Nullable Inventory getInventory(InventoryView view, int rawSlot) {
        // Slot may be -1 if not properly detected due to client bug
        // e.g. dropping an item into part of the enchantment list section of an enchanting table
        if (rawSlot == InventoryView.OUTSIDE || rawSlot == -1) {
            return null;
        }
        Preconditions.checkArgument(rawSlot >= 0, "Negative, non outside slot %s", rawSlot);
        Preconditions.checkArgument(rawSlot < view.countSlots(), "Slot %s greater than inventory slot count (%s - %s)", rawSlot, view.countSlots(), view.getTopInventory().getType());

        if (rawSlot < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }
	

}
