package me.szumielxd.portfel.common.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import me.lucko.jarrelocator.JarRelocator;

public class DependencyLoader {
	
	
	private final PortfelBootstrap plugin;
	
	private final Path REPO_DIR;
	private final Path REPO_CACHE_DIR;
	private final String LIBS_PATH;
	private final Path REAL_JAR;
	
	
	public DependencyLoader(@NotNull PortfelBootstrap plugin) {
		this.plugin = plugin;
		
		REPO_DIR = new File(this.plugin.getDataFolder(), "libs").toPath();
		REPO_CACHE_DIR = Paths.get(REPO_DIR.toString(), "cache");
		LIBS_PATH = Optional.of(this.plugin.getClass().getName().substring(0, this.plugin.getClass().getName().lastIndexOf('.')))
				.map(str -> str.substring(0, str.lastIndexOf('.'))).orElseThrow(NullPointerException::new);
		REAL_JAR = Paths.get(REPO_DIR.toString(), plugin.getName().toLowerCase() + "-origin.jar");
	}
	
	
	public JarClassLoader load(@NotNull ClassLoader parentClassLoader) {
		return this.load(parentClassLoader, EnumSet.allOf(CommonDependency.class));
	}
	
	
	public JarClassLoader load(@NotNull ClassLoader parentClassLoader, @NotNull CommonDependency... dependencies) {
		Objects.requireNonNull(dependencies, "dependencies cannot be null");
		Stream.of(dependencies).forEach(dep -> Objects.requireNonNull(dep, "dependencies cannot be null"));
		if (dependencies.length == 0) return this.load(parentClassLoader, new HashSet<>());
		return this.load(parentClassLoader, EnumSet.copyOf(Arrays.asList(dependencies)));
	}
	
	
	public JarClassLoader load(@NotNull ClassLoader parentClassLoader, @NotNull Set<CommonDependency> dependencies) {
		try {
			Files.createDirectories(REPO_DIR);
			Files.createDirectories(REPO_CACHE_DIR);
		} catch (IOException e) {
			e.printStackTrace();
		}
		dependencies = dependencies.isEmpty()? new HashSet<>() : EnumSet.copyOf(dependencies);
		dependencies.removeIf(d -> !d.getVersionRange().isApplicable());
		try {
			Files.copy(this.getClass().getClassLoader().getResourceAsStream(String.format("%s.jarinjar", this.plugin.getName().toLowerCase())), REAL_JAR, 
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		dependencies.parallelStream().filter(d -> !validateDependency(d)).forEach(d -> {
			try {
				download(d);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		if (Files.exists(REPO_CACHE_DIR)) {
			try (Stream<Path> fileStream = Files.walk(REPO_CACHE_DIR)) {
				fileStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.loadDependencies(dependencies, parentClassLoader);
	}
	
	

	
	
	public void addToLoader(@NotNull JarClassLoader classLoader, @NotNull CommonDependency... dependencies) {
		Objects.requireNonNull(dependencies, "dependencies cannot be null");
		Stream.of(dependencies).forEach(dep -> Objects.requireNonNull(dep, "dependencies cannot be null"));
		if (dependencies.length == 0) this.addToLoader(classLoader, new HashSet<>());
		this.addToLoader(classLoader, EnumSet.copyOf(Arrays.asList(dependencies)));
	}
	
	
	public void addToLoader(@NotNull JarClassLoader classLoader, @NotNull Set<CommonDependency> dependencies) {
		try {
			Files.createDirectories(REPO_DIR);
			Files.createDirectories(REPO_CACHE_DIR);
		} catch (IOException e) {
			e.printStackTrace();
		}
		dependencies = dependencies.isEmpty()? new HashSet<>() : EnumSet.copyOf(dependencies);
		dependencies.removeIf(d -> !d.getVersionRange().isApplicable());
		dependencies.parallelStream().filter(d -> !validateDependency(d)).forEach(d -> {
			try {
				download(d);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		if (Files.exists(REPO_CACHE_DIR)) {
			try (Stream<Path> fileStream = Files.walk(REPO_CACHE_DIR)) {
				fileStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.appendDependencies(dependencies, classLoader);
	}
	
	
	
	
	
	private boolean validateDependency(@NotNull CommonDependency dependency) {
		Path path = Paths.get(REPO_DIR.toString(), dependency.getFileName());
		if (!Files.exists(path)) return false;
		try (JarFile jar = new JarFile(path.toFile())) {
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
	
	
	private void download(@NotNull CommonDependency dependency) throws IOException {
		this.plugin.getCommonLogger().info(String.format("Downloading %s lib...", dependency.getArtifactId()));
		long start = System.currentTimeMillis();
		Path cache = Paths.get(REPO_CACHE_DIR.toString(), UUID.randomUUID().toString());
		try (ReadableByteChannel ch = Channels.newChannel(new URL(buildDownloadUrl(dependency)).openStream());
				FileOutputStream out = new FileOutputStream(cache.toString())) {
			out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
			Path destPath = Paths.get(REPO_DIR.toString(), dependency.getFileName());
			new JarRelocator(cache.toFile(), destPath.toFile(), Stream.of(dependency.getGroupPaths()).collect(Collectors.toMap(Function.identity(), path -> LIBS_PATH + ".lib." + path))).run();
			try (FileSystem jar = FileSystems.newFileSystem(URI.create("jar:" + destPath.toFile().toURI()), Collections.singletonMap("create", "true"))) {
				try (Writer writer = Files.newBufferedWriter(jar.getPath("checksum.md5"), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
					writer.write(dependency.getMd5());
				}
			}
		}
		this.plugin.getCommonLogger().info("Successfully downloaded %s! (%s ms)", dependency.getArtifactId(), System.currentTimeMillis() - start);
	}
	
	
	private JarClassLoader loadDependencies(@NotNull Set<CommonDependency> dependencies, @NotNull ClassLoader parentClassLoader) {
		List<URL> list = new ArrayList<>();
		try {
			list.add(REAL_JAR.toUri().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		dependencies.stream().map(d -> {
			try {
				return Paths.get(REPO_DIR.toString(), d.getFileName()).toUri().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}).forEach(list::add);
		return new JarClassLoader(list.toArray(new URL[0]), parentClassLoader);
	}
	
	
	private void appendDependencies(@NotNull Set<CommonDependency> dependencies, @NotNull JarClassLoader classLoader) {
		List<URL> list = new ArrayList<>();
		try {
			list.add(REAL_JAR.toUri().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		dependencies.stream().map(d -> {
			try {
				return Paths.get(REPO_DIR.toString(), d.getFileName()).toUri().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}).forEach(list::add);
		classLoader.appendUrls(list.toArray(new URL[0]));
	}
	
	
	private String buildDownloadUrl(CommonDependency dependency) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(dependency.getDownloadPath() + "maven-metadata.xml").openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36 OPR/83.0.4254.66");
			if (conn.getResponseCode() == 200) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document xml = builder.parse(conn.getInputStream());
				Element versioning = (Element) xml.getElementsByTagName("versioning").item(0);
				Element snapshots = ((Element) xml.getElementsByTagName("snapshotVersions").item(0));
				if (snapshots != null) {
					NodeList nodes = snapshots.getElementsByTagName("snapshotVersion");
					String lastUpdated = versioning.getElementsByTagName("timestamp").item(0).getTextContent().replace(".", "");
					for (int i = 0; i < nodes.getLength(); i++) {
						if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element version = (Element) nodes.item(i);
							if (version.getElementsByTagName("extension").item(0).getTextContent().equals("jar")
								&& version.getElementsByTagName("updated").item(0).getTextContent().equals(lastUpdated)
								&& version.getElementsByTagName("classifier").getLength() == 0) {
									return dependency.getDownloadPath() + dependency.getArtifactId() + "-" + version.getElementsByTagName("value").item(0).getTextContent() + ".jar";
							}
						}
					}
				}
				
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		return dependency.getDownloadPath() + dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
	}
	
	
	
	

}
