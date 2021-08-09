package me.szumielxd.portfel.bukkit.objects;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class OrderData {
	
	
	private final String orderName;
	private final long price;
	private final List<String> broadcast;
	private final List<String> message;
	private final List<String> command;
	
	
	public OrderData(@NotNull String orderName, long price, @NotNull List<String> broadcast, @NotNull List<String> message, @NotNull List<String> command) {
		this.orderName = orderName;
		this.price = price;
		this.broadcast = Collections.unmodifiableList(broadcast);
		this.message = Collections.unmodifiableList(message);
		this.command = Collections.unmodifiableList(command);
	}
	
	
	public @NotNull String getName() {
		return this.orderName;
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
