package me.szumielxd.portfel.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

public class Config {
	
	
	public static enum ConfigKey implements AbstractKey {
		
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
	
	
	public static interface AbstractKey {
		
		public String getPath();
		
		public Object getDefault();
		
		public Class<?> getType();
		
	}
	
	
	private final Portfel plugin;
	private final YamlFile yaml;
	public Config(Portfel plugin) {
		this.plugin = plugin;
		this.yaml = new YamlFile(new File(this.plugin.getDataFolder(), "config.yml"));
	}
	
	
	public Config init(AbstractKey... values) {
		this.yaml.addDefaults(Stream.of(values).collect(Collectors.toMap(AbstractKey::getPath, AbstractKey::getDefault)));
		try {
			this.yaml.createOrLoadWithComments();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	
	public @NotNull String getString(@NotNull AbstractKey key) {
		return this.yaml.isString(key.getPath()) ? this.yaml.getString(key.getPath()) : (String) key.getDefault();
	}
	
	
	@SuppressWarnings("unchecked")
	public @NotNull List<String> getStringList(@NotNull AbstractKey key) {
		return this.yaml.isList(key.getPath()) ? this.yaml.getStringList(key.getPath()) : (List<String>) key.getDefault();
	}
	
	
	public int getInt(@NotNull AbstractKey key) {
		return this.yaml.isInt(key.getPath()) ? this.yaml.getInt(key.getPath()) : (int) key.getDefault();
	}
	
	
	public Map<String, String> getStringMap(@NotNull AbstractKey key) {
		if (this.yaml.isConfigurationSection(key.getPath())) {
			final ConfigurationSection cfg = this.yaml.getConfigurationSection(key.getPath());
			return cfg.getKeys(false).stream().collect(Collectors.toMap(k -> k, k -> cfg.getString(k)));
		}
		return new HashMap<>();
	}
	

}
