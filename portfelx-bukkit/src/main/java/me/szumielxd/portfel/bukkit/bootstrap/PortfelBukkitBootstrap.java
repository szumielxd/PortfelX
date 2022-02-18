package me.szumielxd.portfel.bukkit.bootstrap;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.plugin.java.JavaPlugin;
import me.szumielxd.portfel.common.loader.DependencyLoader;
import me.szumielxd.portfel.common.loader.JarClassLoader;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.loader.PortfelBootstrap;

public class PortfelBukkitBootstrap extends JavaPlugin implements PortfelBootstrap {
	
	
	private LoadablePortfel realPlugin;
	private DependencyLoader dependencyLoader;
	private JarClassLoader jarClassLoader;
	
	
	@Override
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.jarClassLoader = this.dependencyLoader.load();
		try {
			Class<?> clazz = this.jarClassLoader.loadClass("me.szumielxd.portfel.bukkit.PortfelBukkitImpl");
			Class<? extends LoadablePortfel> subClazz = clazz.asSubclass(LoadablePortfel.class);
			this.realPlugin = subClazz.getConstructor(PortfelBukkitBootstrap.class).newInstance(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		this.realPlugin.onLoad();
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
