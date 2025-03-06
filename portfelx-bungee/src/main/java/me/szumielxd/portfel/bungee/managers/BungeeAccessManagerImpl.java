package me.szumielxd.portfel.bungee.managers;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Listener;

public class BungeeAccessManagerImpl extends AccessManagerImpl<PortfelBungeeImpl, BaseComponent[]> implements Listener {

	public BungeeAccessManagerImpl(@NotNull PortfelBungeeImpl plugin) {
		super(plugin);
	}

	@Override
	protected void preInit() {
		// nothing to do
	}

	@Override
	protected void postInit() {
		// nothing to do
	}
	

}
