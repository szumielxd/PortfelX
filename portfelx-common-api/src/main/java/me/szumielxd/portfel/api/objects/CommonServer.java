package me.szumielxd.portfel.api.objects;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonServer<C> {
	
	
	public @Nullable CommonPlayer<C> getPlayer(@NotNull UUID uuid);
	
	public @Nullable CommonPlayer<C> getPlayer(@NotNull String name);
	
	public @NotNull Collection<? extends CommonPlayer<C>> getPlayers();
	
	public @NotNull CommonSender<C> getConsole();
	

}
