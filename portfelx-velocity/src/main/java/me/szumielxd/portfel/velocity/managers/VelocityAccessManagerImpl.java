package me.szumielxd.portfel.velocity.managers;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import net.kyori.adventure.text.Component;

public class VelocityAccessManagerImpl extends AccessManagerImpl<PortfelVelocityImpl, Component> {

	public VelocityAccessManagerImpl(@NotNull PortfelVelocityImpl plugin) {
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
