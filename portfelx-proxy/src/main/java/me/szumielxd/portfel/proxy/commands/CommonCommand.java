package me.szumielxd.portfel.proxy.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.proxy.api.objects.ProxySender;

public abstract class CommonCommand {
	
	
	private final @NotNull String name;
	private final @Nullable String permission;
	private final @NotNull String[] aliases;
	
	
	public CommonCommand(@NotNull String name, @Nullable String permission, @NotNull String... aliases) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.permission = permission;
		this.aliases = Arrays.copyOf(Objects.requireNonNull(aliases, "aliases cannot be null"), aliases.length);
	}
	
	
	public abstract void execute(@NotNull ProxySender sender, @NotNull String[] args);
	
	
	public @NotNull List<String> onTabComplete(@NotNull ProxySender sender, @NotNull String[] args) {
		return new ArrayList<>();
	}
	
	
	public @NotNull String getName() {
		return this.name;
	}
	
	public @Nullable String getPermission() {
		return this.permission;
	}
	
	public @NotNull String[] getAliases() {
		return this.aliases;
	}
	

}
