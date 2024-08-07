package me.szumielxd.portfel.common.commands.common;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.SimpleCommand;

public interface ParentLikeCommand<C> extends AbstractCommand<C> {
	
	public @NotNull Collection<SimpleCommand<C>> getChildrens();

}
