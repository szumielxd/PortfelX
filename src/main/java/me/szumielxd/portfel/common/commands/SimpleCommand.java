package me.szumielxd.portfel.common.commands;

import org.jetbrains.annotations.NotNull;

public abstract class SimpleCommand implements AbstractCommand {
	
	
	private final AbstractCommand parent;
	private final String name;
	private final String permission;
	private final String[] aliases;
	
	
	public SimpleCommand(@NotNull AbstractCommand parent, @NotNull String name, @NotNull String... aliases) {
		this.name = name;
		this.aliases = aliases;
		this.permission = parent.getPermission() + "." + this.name;
		this.parent = parent;
	}
	

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public @NotNull String[] getAliases() {
		return this.aliases.clone();
	}

	@Override
	public @NotNull String getPermission() {
		return this.permission;
	}
	
	public @NotNull AbstractCommand getParent() {
		return this.parent;
	}

}
