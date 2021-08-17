
package me.szumielxd.portfel.bungee.api.objects;

import me.szumielxd.portfel.api.PortfelProvider;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeeSender;
import net.md_5.bungee.api.CommandSender;

public class BungeeSenderWrapper {
	
	
	public static CommonSender get(CommandSender sender) {
		return BungeeSender.get((PortfelBungeeImpl) PortfelProvider.get(), sender);
	}
	

}
