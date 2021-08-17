package me.szumielxd.portfel.api.configuration;

import org.jetbrains.annotations.NotNull;

public enum ConfigKey implements AbstractKey {
	
	LANG_DEFAULT_LOCALE("lang.default-locale", "en_US"),
	;
	
	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private ConfigKey(@NotNull String path, @NotNull Object defaultValue) {
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
