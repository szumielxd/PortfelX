package me.szumielxd.portfel.common.commands.common;

import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.SimpleCommand;

public interface ParentLikeCommand<C> extends AbstractCommand<C> {
	
	public @NotNull Collection<SimpleCommand<C>> getChildrens();
	
	public @NotNull Optional<SimpleCommand<C>> getChildren(@Nullable String name);

}
