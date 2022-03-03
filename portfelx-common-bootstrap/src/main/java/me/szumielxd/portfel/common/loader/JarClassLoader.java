package me.szumielxd.portfel.common.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class JarClassLoader extends URLClassLoader {
	
	static {
		ClassLoader.registerAsParallelCapable();
	}
	

	public JarClassLoader(@NotNull URL[] urls, @NotNull ClassLoader parent) {
		super(urls, parent);
	}
	
	
	public void appendUrls(@NotNull URL... urls) {
		Stream.of(urls).forEachOrdered(obj -> Objects.requireNonNull(obj, "urls array cannot contain null elements."));
		Stream.of(urls).forEachOrdered(url -> this.addURL(url));
	}

}
