package me.szumielxd.portfel.common.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jetbrains.annotations.NotNull;

import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import dev.vankka.dependencydownload.dependency.StandardDependency;
import dev.vankka.dependencydownload.relocation.Relocation;
import dev.vankka.dependencydownload.repository.StandardRepository;

public class OldDependencyLoader {
	
	
	private final @NotNull File pluginDir;
	
	
	public OldDependencyLoader(@NotNull File pluginDir) {
		this.pluginDir = Objects.requireNonNull(pluginDir, "pluginDir cannot be null");
	}
	
	
	
	
	public void loadDependency() {
		
		DependencyManager manager = new DependencyManager(new File(pluginDir, "cache").toPath());
		manager.addDependency(new StandardDependency("com.example", "examplepackage", "1.0.0", "<hash>", "SHA-256"));
		manager.addRelocation(new Relocation("com.example", "relocated.com.example", null, null));
		
		Executor executor = Executors.newCachedThreadPool();
		
		manager.downloadAll(executor, Collections.singletonList(new StandardRepository("https://repo.example.com/maven2"))).join();
		manager.relocateAll(executor).join();
		//manager.loadAll(executor, classpathAppender).join(); // ClasspathAppender is a interface that you need to implement to append a Path to the classpath
		
		new ClasspathAppender() {

			@Override
			public void appendFileToClasspath(Path path) throws MalformedURLException {
				// TODO Auto-generated method stub
				this.getClass().getClassLoader();
				Thread.currentThread().setContextClassLoader(null);
			}
			
		};
		
	}
	

}
