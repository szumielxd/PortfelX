package me.szumielxd.portfel.bukkit.objects;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;

public class OrderData {
	
	
	protected final String orderName;
	protected final int slot;
	protected final int level;
	protected final Component display;
	protected final List<String> description;
	protected final ItemStack icon;
	protected final ItemStack iconBought;
	protected final long price;
	protected final String donePermission;
	protected final List<String> broadcast;
	protected final List<String> message;
	protected final List<String> command;
	
	
	public OrderData(@NotNull String orderName, int slot, int level, @NotNull Component display, List<String> description, @NotNull ItemStack icon, @NotNull ItemStack iconBought, long price, @Nullable String donePermission, @NotNull List<String> broadcast, @NotNull List<String> message, @NotNull List<String> command) {
		this.orderName = orderName;
		this.slot = slot;
		this.level = level;
		this.display = display;
		this.description = Collections.unmodifiableList(description);
		this.icon = icon;
		this.iconBought = iconBought;
		this.price = price;
		this.donePermission = donePermission;
		this.broadcast = Collections.unmodifiableList(broadcast);
		this.message = Collections.unmodifiableList(message);
		this.command = Collections.unmodifiableList(command);
	}
	
	
	public @NotNull String getName() {
		return this.orderName;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public @NotNull Component getDisplay() {
		return this.display;
	}
	
	public @NotNull List<String> getDescription() {
		return this.description;
	}
	
	public @NotNull ItemStack getIcon() {
		return this.icon;
	}
	
	public @NotNull ItemStack getIconBought() {
		return this.iconBought;
	}
	
	public @Nullable String getDonePermission() {
		return this.donePermission;
	}
	
	public long getPrice() {
		return this.price;
	}
	
	public @NotNull List<String> getBroadcast() {
		return this.broadcast;
	}
	
	public @NotNull List<String> getMessage() {
		return this.message;
	}
	
	public @NotNull List<String> getCommand() {
		return this.command;
	}
	
	public boolean isAvailable(@NotNull Player player) {
		return this.donePermission == null || player.hasPermission(orderName);
	}
	
	public OrderDataOnAir onAirWithPrice(long price) {
		return new OrderDataOnAir(this.orderName, this.display, price, this.broadcast, this.message, this.command);
	}
	
	public static final class OrderDataOnAir {
		
		
		protected final String orderName;
		protected final Component display;
		protected final long price;
		protected final List<String> broadcast;
		protected final List<String> message;
		protected final List<String> command;
		

		public OrderDataOnAir(@NotNull String orderName, @NotNull Component display, long price, @NotNull List<String> broadcast, @NotNull List<String> message, @NotNull List<String> command) {
			this.orderName = orderName;
			this.display = display;
			this.price = price;
			this.broadcast = Collections.unmodifiableList(broadcast);
			this.message = Collections.unmodifiableList(message);
			this.command = Collections.unmodifiableList(command);
		}
		
		
		public @NotNull String getName() {
			return this.orderName;
		}
		
		public @NotNull Component getDisplay() {
			return this.display;
		}
		
		public long getPrice() {
			return this.price;
		}
		
		public @NotNull List<String> getBroadcast() {
			return this.broadcast;
		}
		
		public @NotNull List<String> getMessage() {
			return this.message;
		}
		
		public @NotNull List<String> getCommand() {
			return this.command;
		}
		
	}
	

}
