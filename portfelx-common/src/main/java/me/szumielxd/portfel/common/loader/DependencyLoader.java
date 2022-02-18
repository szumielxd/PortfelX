package me.szumielxd.portfel.common.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.jetbrains.annotations.NotNull;

import me.lucko.jarrelocator.JarRelocator;

public class DependencyLoader {
	
	
	private final PortfelBootstrap plugin;
	
	private final File REPO_DIR;
	private final File REPO_CACHE_DIR;
	private final String LIBS_PATH;
	
	
	public DependencyLoader(@NotNull PortfelBootstrap plugin) {
		this.plugin = plugin;
		
		REPO_DIR = new File(this.plugin.getDataFolder(), "libs");
		REPO_CACHE_DIR = new File(REPO_DIR, "cache");
		LIBS_PATH = this.plugin.getClass().getName().substring(0, this.plugin.getClass().getName().lastIndexOf('.'));
	}
	
	
	public JarClassLoader load() {
		return this.load(EnumSet.allOf(CommonDependency.class));
	}
	
	
	public JarClassLoader load(@NotNull CommonDependency... dependencies) {
		Objects.requireNonNull(dependencies, "dependencies cannot be null");
		Stream.of(dependencies).forEach(dep -> Objects.requireNonNull(dep, "dependencies cannot be null"));
		if (dependencies.length == 0) return this.load(new HashSet<>());
		return this.load(EnumSet.copyOf(Arrays.asList(dependencies)));
	}
	
	
	public JarClassLoader load(@NotNull Set<CommonDependency> dependencies) {
		if (!REPO_DIR.exists()) REPO_DIR.mkdirs();
		if (!REPO_CACHE_DIR.exists()) REPO_CACHE_DIR.mkdirs();
		dependencies = dependencies.isEmpty()? new HashSet<>() : EnumSet.copyOf(dependencies);
		try {
			Files.copy(Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()), new File(REPO_DIR, plugin.getName().toLowerCase() + "-origin.jar").toPath(), 
					StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (URISyntaxException | IOException e) {
			throw new IllegalStateException(e);
		}
		dependencies.parallelStream().filter(d -> !validateDependency(d)).forEach(d -> {
			try {
				download(d);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		if (REPO_CACHE_DIR.exists()) {
			try {
				Files.walk(REPO_CACHE_DIR.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.loadDependencies(dependencies);
	}
	
	
	private boolean validateDependency(@NotNull CommonDependency dependency) {
		File f = new File(REPO_DIR, dependency.getFileName());
		if (!f.exists()) return false;
		try (JarFile jar = new JarFile(f)) {
			ZipEntry entry = jar.getEntry("checksum.md5");
			if (entry == null) return false;
			try (InputStream is = jar.getInputStream(entry)) {
				byte[] checksum = new byte[is.available()];
				is.read(checksum);
				return dependency.getMd5().equals(new String(checksum, StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	
	private void download(@NotNull CommonDependency dependency) throws MalformedURLException, IOException {
		this.plugin.getLogger().info(String.format("Downloading %s lib...", dependency.getArtifactId()));
		long start = System.currentTimeMillis();
		File cache = new File(REPO_CACHE_DIR, UUID.randomUUID().toString());
		try (ReadableByteChannel ch = Channels.newChannel(new URL(dependency.getDownloadUrl()).openStream());
				FileOutputStream out = new FileOutputStream(cache)) {
			out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
			File f = new File(REPO_DIR, dependency.getFileName());
			new JarRelocator(cache, f, Collections.singletonMap(dependency.getGroupId(), LIBS_PATH + ".lib." + dependency.getGroupId())).run();
			try (FileSystem jar = FileSystems.newFileSystem(URI.create("jar:" + f.toURI()), Collections.singletonMap("create", "true"))) {
				try (Writer writer = Files.newBufferedWriter(jar.getPath("checksum.md5"), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(dependency.getMd5());
				}
			}
		}
		this.plugin.getLogger().info(String.format("Successfully downloaded %s! (%s ms)", dependency.getArtifactId(), System.currentTimeMillis() - start));
	}
	
	
	private JarClassLoader loadDependencies(@NotNull Set<CommonDependency> dependencies) {
		List<URL> list = new ArrayList<>();
		new HashSet<>(dependencies).parallelStream().map(d -> {
			try {
				return new File(REPO_DIR, d.getFileName()).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}).forEach(list::add);
		try {
			list.add(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toURL());
		} catch (MalformedURLException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return new JarClassLoader(list.toArray(new URL[0]), getClass().getClassLoader());
	}
	
	
	
	

}
