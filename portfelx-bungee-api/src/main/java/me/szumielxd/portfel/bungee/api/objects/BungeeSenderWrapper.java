
package me.szumielxd.portfel.bungee.api.objects;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.objects.CommonSender;
import net.md_5.bungee.api.CommandSender;

public class BungeeSenderWrapper {
	
	
	public static CommonSender get(@NotNull CommandSender sender) {
		try {
			return (CommonSender) Class.forName("me.szumielxd.portfel.bungee.objects.BungeeSender")
					.getMethod("get", PortfelProvider.get().getClass(), CommandSender.class)
					.invoke(null, PortfelProvider.get(), sender);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException("PortfelBukkit is probably not initialized", e);
		}
	}
	

}
