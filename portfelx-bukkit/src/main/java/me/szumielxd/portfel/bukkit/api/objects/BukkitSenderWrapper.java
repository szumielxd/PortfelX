package me.szumielxd.portfel.bukkit.api.objects;

import org.bukkit.command.CommandSender;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;

public class BukkitSenderWrapper {
	
	
	public static CommonSender get(CommandSender sender) {
		return BukkitSender.get((PortfelBukkitImpl) PortfelProvider.get(), sender);
	}
	

}
