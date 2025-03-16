package me.szumielxd.portfel.api.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonServer<C> extends CommonGroupAudience<C> {
	
	
	public @Nullable CommonPlayer<C> getPlayer(@NotNull UUID uuid);
	
	public @Nullable CommonPlayer<C> getPlayer(@NotNull String name);
	
	public @NotNull Collection<? extends CommonPlayer<C>> getPlayers();
	
	public @NotNull CommonSender<C> getConsole();
	
	public default @NotNull Collection<CommonAudience<C>> getAudience() {
		List<CommonAudience<C>> list = new LinkedList<>();
		list.add(getConsole());
		list.addAll(getPlayers());
		return list;
	}
	

}
