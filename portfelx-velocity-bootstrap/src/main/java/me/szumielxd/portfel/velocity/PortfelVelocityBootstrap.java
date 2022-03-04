package me.szumielxd.portfel.velocity;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.loader.CommonLogger;
import me.szumielxd.portfel.common.loader.DependencyLoader;
import me.szumielxd.portfel.common.loader.JarClassLoader;
import me.szumielxd.portfel.common.loader.LoadablePortfel;
import me.szumielxd.portfel.common.loader.PortfelBootstrap;

import static me.szumielxd.portfel.common.loader.CommonDependency.*;

@Plugin(
		id = "id----",
		name = "@pluginName@",
		version = "@version@",
		authors = { "@author@" },
		description = "@description@",
		url = "https://github.com/szumielxd/PortfelX/",
		dependencies = {
				@Dependency( id="luckperms", optional=true )
		}
)
public class PortfelVelocityBootstrap implements PortfelBootstrap {
	
	
	private LoadablePortfel realPlugin;
	private DependencyLoader dependencyLoader;
	private JarClassLoader jarClassLoader;
	
	
	private final ProxyServer server;
	private final CommonLogger logger;
	private final File dataFolder;
	
	
	@Inject
	public PortfelVelocityBootstrap(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = new VelocityLogger(logger);
		this.dataFolder = dataDirectory.toFile();
	}
	
	
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.jarClassLoader = this.dependencyLoader.load(getClass().getClassLoader(), HIKARICP4, HIKARICP5, GSON, RGXGEN, YAML);
		try {
			Class<?> clazz = this.jarClassLoader.loadClass("me.szumielxd.portfel.velocity.PortfelVelocityImpl");
			this.realPlugin = clazz.asSubclass(LoadablePortfel.class).getConstructor(PortfelVelocityBootstrap.class).newInstance(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		this.realPlugin.onLoad();
	}
	
	
	public void addToRuntime(CommonDependency... dependency) {
		this.dependencyLoader.addToLoader(jarClassLoader, dependency);
	}
	
	

	
	
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		this.onLoad();
		this.realPlugin.onEnable();
	}
	
	
	@Subscribe
	public void onProxyInitialization(ProxyShutdownEvent event) {
		this.realPlugin.onDisable();
	}


	@Override
	public @NotNull File getDataFolder() {
		return this.dataFolder;
	}


	@Override
	public @NotNull CommonLogger getCommonLogger() {
		return this.logger;
	}


	@Override
	public @NotNull String getName() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this).getDescription().getName().orElse("");
	}
	
	
	public @NotNull ProxyServer getProxy() {
		return this.server;
	}
	

}
