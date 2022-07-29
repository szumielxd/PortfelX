package me.szumielxd.portfel.common.loader;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

public interface PortfelBootstrap {
	
	
	public @NotNull Path getDataFolderPath();
	
	public @NotNull CommonLogger getCommonLogger();
	
	public @NotNull String getName();
	

}
