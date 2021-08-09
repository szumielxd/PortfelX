package me.szumielxd.portfel.bukkit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Config.AbstractKey;

public enum BukkitConfigKey implements AbstractKey {
	
	SERVER_NAME("server.name", "UNKNOWN"),
	;

	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private BukkitConfigKey(@NotNull String path, @NotNull Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.type = defaultValue.getClass();
	}
	
	public String getPath() {
		return this.path;
	}
	
	public Object getDefault() {
		return this.defaultValue;
	}
	
	public Class<?> getType() {
		return this.type;
	}

}
