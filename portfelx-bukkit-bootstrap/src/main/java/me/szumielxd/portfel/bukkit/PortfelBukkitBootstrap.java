package me.szumielxd.portfel.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.CommonLogger;
import me.szumielxd.portfel.common.loader.DependencyLoader;
import me.szumielxd.portfel.common.loader.JarClassLoader;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.loader.PortfelBootstrap;

import static me.szumielxd.portfel.common.loader.CommonDependency.*;

public class PortfelBukkitBootstrap extends JavaPlugin implements PortfelBootstrap {
	
	
	private LoadablePortfel realPlugin;
	private DependencyLoader dependencyLoader;
	private JarClassLoader jarClassLoader;
	private CommonLogger commonLogger;
	
	
	@Override
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.commonLogger = new BukkitLogger(this.getLogger());
		this.jarClassLoader = this.dependencyLoader.load(getClass().getClassLoader(), GSON, RGXGEN, YAML, ADVENTURE_PLATFORM_BUKKIT);
		try {
			Class<?> clazz = this.jarClassLoader.loadClass("me.szumielxd.portfel.bukkit.PortfelBukkitImpl");
			this.realPlugin = clazz.asSubclass(LoadablePortfel.class).getConstructor(PortfelBukkitBootstrap.class).newInstance(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		this.realPlugin.onLoad();
	}
	
	
	public void addToRuntime(CommonDependency... dependency) {
		this.dependencyLoader.addToLoader(jarClassLoader, dependency);
	}
	
	
	@Override
	public void onEnable() {
		this.realPlugin.onEnable();
	}
	
	
	@Override
	public void onDisable() {
		this.realPlugin.onDisable();
	}
	
	
	@Override
	public @NotNull CommonLogger getCommonLogger() {
		if (this.commonLogger == null) throw new IllegalStateException("plugin hasn't been already initialized");
		return this.commonLogger;
	}


	@Override
	public @NotNull Path getDataFolderPath() {
		return this.getDataFolder().toPath();
	}
	

}
