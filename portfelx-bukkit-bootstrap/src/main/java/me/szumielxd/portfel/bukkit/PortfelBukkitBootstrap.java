package me.szumielxd.portfel.bukkit;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.plugin.java.JavaPlugin;
import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.DependencyLoader;
import me.szumielxd.portfel.common.loader.JarClassLoader;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.loader.PortfelBootstrap;

import static me.szumielxd.portfel.common.loader.CommonDependency.*;

public class PortfelBukkitBootstrap extends JavaPlugin implements PortfelBootstrap {
	
	
	private LoadablePortfel realPlugin;
	private DependencyLoader dependencyLoader;
	private JarClassLoader jarClassLoader;
	
	
	@Override
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.jarClassLoader = this.dependencyLoader.load(getClass().getClassLoader(), HIKARICP4, HIKARICP5, GSON, RGXGEN, YAML);
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
	

}
