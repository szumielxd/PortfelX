package me.szumielxd.portfel.bukkit.api.objects;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.command.CommandSender;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.objects.CommonSender;

public class BukkitSenderWrapper {
	
	
	public static CommonSender get(CommandSender sender) {
		try {
			return (CommonSender) Class.forName("me.szumielxd.portfel.bukkit.objects.BukkitSender").getMethod(null, PortfelProvider.get().getClass(), CommonSender.class).invoke(null, PortfelProvider.get(), sender);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException("PortfelBukkit is probably not initialized", e);
		}
	}
	

}
