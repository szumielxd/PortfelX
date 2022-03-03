package me.szumielxd.portfel.bungee;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.DependencyLoader;
import me.szumielxd.portfel.common.loader.JarClassLoader;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.loader.PortfelBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

import static me.szumielxd.portfel.common.loader.CommonDependency.*;

public class PortfelBungeeBootstrap extends Plugin implements PortfelBootstrap {
	
	
	private LoadablePortfel realPlugin;
	private DependencyLoader dependencyLoader;
	private JarClassLoader jarClassLoader;
	
	
	@Override
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.jarClassLoader = this.dependencyLoader.load(getClass().getClassLoader(), HIKARICP4, HIKARICP5, GSON, RGXGEN, YAML);
		try {
			Class<?> clazz = this.jarClassLoader.loadClass("me.szumielxd.portfel.bungee.PortfelBungeeImpl");
			this.realPlugin = clazz.asSubclass(LoadablePortfel.class).getConstructor(PortfelBungeeBootstrap.class).newInstance(this);
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
	public @NotNull String getName() {
		return this.getDescription().getName();
	}
	

}
