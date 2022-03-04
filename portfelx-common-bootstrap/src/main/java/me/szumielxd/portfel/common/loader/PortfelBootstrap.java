package me.szumielxd.portfel.common.loader;

import java.io.File;
import org.jetbrains.annotations.NotNull;

public interface PortfelBootstrap {
	
	
	public @NotNull File getDataFolder();
	
	public @NotNull CommonLogger getCommonLogger();
	
	public @NotNull String getName();
	

}
