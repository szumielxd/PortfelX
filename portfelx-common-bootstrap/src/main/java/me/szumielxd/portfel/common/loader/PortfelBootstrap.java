package me.szumielxd.portfel.common.loader;

import java.io.File;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

public interface PortfelBootstrap {
	
	
	public @NotNull File getDataFolder();
	
	public @NotNull Logger getLogger();
	
	public @NotNull String getName();
	

}
