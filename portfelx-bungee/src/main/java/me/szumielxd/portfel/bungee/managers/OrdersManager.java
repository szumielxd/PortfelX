package me.szumielxd.portfel.bungee.managers;

import java.io.File;

import me.szumielxd.portfel.bungee.PortfelBungee;

public class OrdersManager {
	
	
	private final PortfelBungee plugin;
	private final File file;
	
	
	public OrdersManager(PortfelBungee plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "orders.yml");
	}
	
	
	public OrdersManager init() {
		return this;
	}
	

}
