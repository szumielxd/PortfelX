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

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.configuration.AbstractKey;
import me.szumielxd.portfel.api.configuration.Config;

public class ConfigImpl implements Config {
	
	
	private final Portfel plugin;
	private final YamlFile yaml;
	public ConfigImpl(@NotNull Portfel plugin) {
		this.plugin = plugin;
		this.yaml = new YamlFile(new File(this.plugin.getDataFolder(), "config.yml"));
	}
	
	
	/**
	 * Initialize configuration
	 * 
	 * @param values to load
	 * @return this object
	 */
	@Override
	public @NotNull ConfigImpl init(@NotNull AbstractKey... values) {
		this.yaml.addDefaults(Stream.of(values).collect(Collectors.toMap(AbstractKey::getPath, AbstractKey::getDefault)));
		try {
			this.yaml.createOrLoadWithComments();
			this.yaml.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * Get value of given key parsed as string
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	@Override
	public @NotNull String getString(@NotNull AbstractKey key) {
		return this.yaml.isString(key.getPath()) ? this.yaml.getString(key.getPath()) : (String) key.getDefault();
	}
	
	/**
	 * Get value of given key parsed as list of strings
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	@Override
	@SuppressWarnings("unchecked")
	public @NotNull List<String> getStringList(@NotNull AbstractKey key) {
		return this.yaml.isList(key.getPath()) ? this.yaml.getStringList(key.getPath()) : (List<String>) key.getDefault();
	}
	
	/**
	 * Get value of given key parsed as integer
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	@Override
	public int getInt(@NotNull AbstractKey key) {
		return this.yaml.isInt(key.getPath()) ? this.yaml.getInt(key.getPath()) : (int) key.getDefault();
	}
	
	/**
	 * Get value of given key parsed as boolean
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	public boolean getBoolean(@NotNull AbstractKey key) {
		return this.yaml.isBoolean(key.getPath()) ? this.yaml.getBoolean(key.getPath()) : (boolean) key.getDefault();
	}
	
	/**
	 * Get value of given key parsed as map of strings
	 * 
	 * @param key the key
	 * @return value of given key
	 */
	@Override
	public Map<String, String> getStringMap(@NotNull AbstractKey key) {
		if (this.yaml.isConfigurationSection(key.getPath())) {
			final ConfigurationSection cfg = this.yaml.getConfigurationSection(key.getPath());
			return cfg.getKeys(false).stream().collect(Collectors.toMap(k -> k, k -> cfg.getString(k)));
		}
		return new HashMap<>();
	}
	

}
