package me.szumielxd.portfel.api.objects;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonServer {
	
	
	public @Nullable CommonPlayer getPlayer(@NotNull UUID uuid);
	
	public @Nullable CommonPlayer getPlayer(@NotNull String name);
	
	public @NotNull Collection<? extends CommonPlayer> getPlayers();
	
	public @NotNull CommonSender getConsole();
	

}
