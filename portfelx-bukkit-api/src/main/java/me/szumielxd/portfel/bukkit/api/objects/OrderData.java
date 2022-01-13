package me.szumielxd.portfel.bukkit.api.objects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;

public class OrderData {
	
	
	protected final @NotNull String orderName;
	protected final int slot;
	protected final int level;
	protected final @NotNull Component display;
	protected final @NotNull List<Component> description;
	protected final @NotNull List<Component> denyDescription;
	protected final @NotNull ItemStack icon;
	protected final @NotNull ItemStack iconBought;
	protected final @NotNull ItemStack iconDenied;
	protected final long price;
	protected final @Nullable String donePermission;
	protected final @NotNull List<DoneCondition> doneConditions;
	protected final @NotNull List<DoneCondition> denyConditions;
	protected final @NotNull List<String> broadcast;
	protected final @NotNull List<String> message;
	protected final @NotNull List<String> command;
	
	
	public OrderData(@NotNull String orderName, int slot, int level, @NotNull Component display, @NotNull List<Component> description, @NotNull List<Component> denyDescription, @NotNull ItemStack icon, @NotNull ItemStack iconBought, @NotNull ItemStack iconDenied, long price, @Nullable String donePermission, @NotNull List<DoneCondition> doneConditions, @NotNull List<DoneCondition> denyConditions, @NotNull List<String> broadcasts, @NotNull List<String> messages, @NotNull List<String> commands) {
		this.orderName = Objects.requireNonNull(orderName, "orderName cannot be null");
		this.slot = slot;
		this.level = level;
		this.display = Objects.requireNonNull(display, "display cannot be null");
		this.description = Collections.unmodifiableList(Objects.requireNonNull(description, "description cannot be null"));
		this.denyDescription = Collections.unmodifiableList(Objects.requireNonNull(denyDescription, "denyDescription cannot be null"));
		this.icon = Objects.requireNonNull(icon, "icon cannot be null");
		this.iconBought = Objects.requireNonNull(iconBought, "iconBought cannot be null");
		this.iconDenied = Objects.requireNonNull(iconDenied, "iconDenied cannot be null");
		this.price = price;
		this.donePermission = donePermission;
		this.doneConditions = Collections.unmodifiableList(Objects.requireNonNull(doneConditions, "doneConditions cannot be null"));
		this.denyConditions = Collections.unmodifiableList(Objects.requireNonNull(denyConditions, "denyConditions cannot be null"));
		this.broadcast = Collections.unmodifiableList(Objects.requireNonNull(broadcasts, "broadcasts cannot be null"));
		this.message = Collections.unmodifiableList(Objects.requireNonNull(messages, "messages cannot be null"));
		this.command = Collections.unmodifiableList(Objects.requireNonNull(commands, "commands cannot be null"));
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
	
	public @NotNull List<Component> getDescription() {
		return this.description;
	}
	
	public @NotNull List<Component> getDenyDescription() {
		return this.denyDescription;
	}
	
	public @NotNull ItemStack getIcon() {
		return this.icon;
	}
	
	public @NotNull ItemStack getIconBought() {
		return this.iconBought;
	}
	
	public @NotNull ItemStack getIconDenied() {
		return this.iconDenied;
	}
	
	public @Nullable String getDonePermission() {
		return this.donePermission;
	}
	
	public @NotNull List<DoneCondition> getDoneConditions() {
		return this.doneConditions;
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
	
	public boolean isAvailableToBuy(@NotNull Player player) {
		if (this.getDonePermission() != null && player.hasPermission(this.getDonePermission())) {
			if (this.doneConditions.parallelStream().allMatch(c -> c.test(player))) return false;
		}
		return true;
	}
	
	public boolean isDenied(@NotNull Player player) {
		if (this.denyConditions.isEmpty()) return false;
		return this.denyConditions.parallelStream().allMatch(c -> c.test(player));
	}
	
	public @NotNull OrderDataOnAir onAirWithPrice(long price) {
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
