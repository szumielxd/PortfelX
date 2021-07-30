package me.szumielxd.portfel.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config {
	
	
	public static enum ConfigKey {
		
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
	
	
	
	public static @Nullable String getString(@NotNull ConfigKey key) {
		return null;
	}
	

}
