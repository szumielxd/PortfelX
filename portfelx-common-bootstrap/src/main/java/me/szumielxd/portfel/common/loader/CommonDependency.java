package me.szumielxd.portfel.common.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public enum CommonDependency {
	
	
	/*MARIADB(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "org.mariadb.jdbc", "mariadb-java-client", "2.7.4", "9ca3eac191f81a8c0062e35de6e35ed8"),
	MYSQL(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "mysql", "mysql-connector-java", "8.0.28", "95cde01c78e7b04e13305338d60e056a", "com.mysql"),
	H2(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "com.h2database", "h2", "2.1.210", "af4adae008b4f91819f078c55dbef025", "org.h2"),
	HIKARICP4(Repository.MAVEN_CENTRAL, new JavaVersionRange(52.0f, 54.0f), "com,zaxxer", "HikariCP", "4.0.3", "e725642926105cd1bbf4ad7fdff5d5a9"),
	HIKARICP5(Repository.MAVEN_CENTRAL, new JavaVersionRange(55.0f, Float.MAX_VALUE), "com,zaxxer", "HikariCP", "5.0.1", "3bc96d2ce8285470da11ec41bff6129f"),
	GSON(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "com,google,code,gson", "gson", "2.9.0", "53fa3e6753e90d931d62cb89580fde2f", new String[0]),
	RGXGEN(Repository.SONATYPE_SNAPSHOTS, JavaVersionRange.ALL, "com,github,curious-odd-man", "rgxgen", "1.4-SNAPSHOT", "9001282c58fcc6acff13e118ab8c1117", "com,github,curiousoddman"),
	YAML(Repository.JITPACK, JavaVersionRange.ALL, "me,carleslc,Simple-YAML", "Simple-Yaml", "1.8", "3af0881b05077ffac4861f47eea38018", "org,simpleyaml", "org,yaml"),
	ADVENTURE_PLATFORM_BUNGEE(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-platform-bungeecord", "4.1.0", "362673bae7a435ba98693485b589c323", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_BUKKIT(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-platform-bukkit", "4.1.0", "9022357d3878482b183a2943de4ed066", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_API(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-platform-api", "4.1.0", "a547973483a351b05d011b8dd5082a2d", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_FACET(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-platform-facet", "4.1.0", "110781c9f639d65deb1dcfd331796f1f", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_BUNGEE(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-bungeecord", "4.1.0", "72e0486e4da445bc0bf76155c8f5e4d4", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_GSON(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-gson", "4.8.0", "b2f45ad565708d86a6c0b4b77b61bb21", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_GSON_LEGACY(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-gson-legacy-impl", "4.8.0", "817d6c558029325ac189d379e492b179", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_LEGACY(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-legacy", "4.10.0", "edb9d7c4c875f77d7ab7ddace2fa34ad", "KYORI_RELOCATIONS"),
	ADVENTURE_API(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-api", "4.10.0", "d7150507993fe6a0faddcedebeb12897", "KYORI_RELOCATIONS"),
	ADVENTURE_NBT(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-nbt", "4.10.0", "960d4803b5e8095e9cee84ef1c7ffa26", "KYORI_RELOCATIONS"),
	ADVENTURE_KEY(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "adventure-key", "4.10.0", "86f4b28385b8e921a1db0b5d05c1f403", "KYORI_RELOCATIONS"),
	EXAMINATION_API(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "examination-api", "1.3.0", "b1887361d811c89ccca4dbf61b88def4", "KYORI_RELOCATIONS"),
	EXAMINATION_STRING(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net,kyori", "examination-string", "1.3.0", "9e4752ea3f53ae45e736c9d8f016f23d", "KYORI_RELOCATIONS"),
	;*/
	MARIADB(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "org.mariadb.jdbc", "mariadb-java-client", "2.7.4", "9ca3eac191f81a8c0062e35de6e35ed8"),
	MYSQL(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "mysql", "mysql-connector-java", "8.0.28", "95cde01c78e7b04e13305338d60e056a", "com.mysql"),
	H2(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "com.h2database", "h2", "2.1.210", "af4adae008b4f91819f078c55dbef025", "org.h2"),
	HIKARICP4(Repository.MAVEN_CENTRAL, new JavaVersionRange(52.0f, 54.0f), "com,zaxxer", "HikariCP", "4.0.3", "e725642926105cd1bbf4ad7fdff5d5a9"),
	HIKARICP5(Repository.MAVEN_CENTRAL, new JavaVersionRange(52.0f, Float.MAX_VALUE), "com,zaxxer", "HikariCP", "5.0.1", "3bc96d2ce8285470da11ec41bff6129f"),
	GSON(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "com,google,code,gson", "gson", "2.9.0", "53fa3e6753e90d931d62cb89580fde2f", "com,google,gson"),
	RGXGEN(Repository.SONATYPE_SNAPSHOTS, JavaVersionRange.ALL, "com,github,curious-odd-man", "rgxgen", "1.4-SNAPSHOT", "9001282c58fcc6acff13e118ab8c1117", "com,github,curiousoddman"),
	YAML(Repository.JITPACK, JavaVersionRange.ALL, "me,carleslc,Simple-YAML", "Simple-Yaml", "1.8", "3af0881b05077ffac4861f47eea38018", "org,simpleyaml", "org,yaml"),
	
	EXAMINATION_API(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "net.kyori", "examination-api", "1.3.0", "b1887361d811c89ccca4dbf61b88def4", "KYORI_RELOCATIONS"),
	EXAMINATION_STRING(Repository.MAVEN_CENTRAL, Arrays.asList(EXAMINATION_API), JavaVersionRange.ALL, "net.kyori", "examination-string", "1.3.0", "9e4752ea3f53ae45e736c9d8f016f23d", "KYORI_RELOCATIONS"),
	
	ADVENTURE_NBT(Repository.MAVEN_CENTRAL, Arrays.asList(EXAMINATION_API, EXAMINATION_STRING), JavaVersionRange.ALL, "net,kyori", "adventure-nbt", "4.11.0", "3cb4f09f86d54c83c7ce641fe9991cb6", "KYORI_RELOCATIONS"),
	ADVENTURE_KEY(Repository.MAVEN_CENTRAL, Arrays.asList(EXAMINATION_API, EXAMINATION_STRING), JavaVersionRange.ALL, "net,kyori", "adventure-key", "4.11.0", "be365d2dff3da990f6ce6e1c4c10cf6f", "KYORI_RELOCATIONS"),
	ADVENTURE_API(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_KEY, EXAMINATION_API, EXAMINATION_STRING), JavaVersionRange.ALL, "net,kyori", "adventure-api", "4.11.0", "544affd2f5dc7f5bc2c80e2e7d9b7d2e", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_GSON(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API, GSON), JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-gson", "4.11.0", "1885f591735dc2c2c798db1208bef661", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_GSON_LEGACY(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API, ADVENTURE_NBT, ADVENTURE_TEXT_GSON), JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-gson-legacy-impl", "4.11.0", "1c28d471d9587c2f15e2228d2b5d15d2", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_LEGACY(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API), JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-legacy", "4.11.0", "d5722b5ab98a44558afebb0f0841976e", "KYORI_RELOCATIONS"),
	ADVENTURE_TEXT_BUNGEE(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API, ADVENTURE_TEXT_GSON, ADVENTURE_TEXT_LEGACY, GSON), JavaVersionRange.ALL, "net,kyori", "adventure-text-serializer-bungeecord", "4.1.1", "72e0486e4da445bc0bf76155c8f5e4d4", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_API(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API), JavaVersionRange.ALL, "net,kyori", "adventure-platform-api", "4.1.1", "430ce5da7657ad00419ebbc0227a9ac2", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_FACET(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_NBT, ADVENTURE_PLATFORM_API), JavaVersionRange.ALL, "net,kyori", "adventure-platform-facet", "4.1.1", "e2083f7767ab6c9366e81a0f328ae376", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_BUNGEE(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API, ADVENTURE_PLATFORM_FACET, ADVENTURE_PLATFORM_API, ADVENTURE_TEXT_GSON_LEGACY, ADVENTURE_TEXT_GSON, ADVENTURE_TEXT_LEGACY, ADVENTURE_TEXT_BUNGEE, GSON, ADVENTURE_NBT), JavaVersionRange.ALL, "net,kyori", "adventure-platform-bungeecord", "4.1.1", "362673bae7a435ba98693485b589c323", "KYORI_RELOCATIONS"),
	ADVENTURE_PLATFORM_BUKKIT(Repository.MAVEN_CENTRAL, Arrays.asList(ADVENTURE_API, ADVENTURE_PLATFORM_FACET, ADVENTURE_PLATFORM_API, ADVENTURE_TEXT_GSON_LEGACY, ADVENTURE_TEXT_GSON, ADVENTURE_TEXT_LEGACY, ADVENTURE_TEXT_BUNGEE, GSON, ADVENTURE_NBT), JavaVersionRange.ALL, "net,kyori", "adventure-platform-bukkit", "4.1.1", "2bb7afb42442d16f5ea90020e80c374b", "KYORI_RELOCATIONS"),
	;
	
	
	@Getter private final String downloadPath;
	@Getter private final Repository repository;
	@Getter private final List<CommonDependency> dependencies;
	@Getter private final JavaVersionRange versionRange;
	@Getter private final String groupId;
	@Getter private final String artifactId;
	@Getter private final String version;
	@Getter private final String md5;
	@Getter private final String[] groupPaths;
	
	private CommonDependency(@NotNull Repository repo, @NotNull JavaVersionRange versionRange, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull String md5) {
		this(repo, versionRange, groupId, artifactId, version, md5, groupId);
	}
	
	private CommonDependency(@NotNull Repository repo, @NotNull JavaVersionRange versionRange, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull String md5, @NotNull String... groupPaths) {
		this(repo, Collections.emptyList(), versionRange, groupId, artifactId, version, md5, groupPaths);
	}
	
	private CommonDependency(@NotNull Repository repo, List<CommonDependency> dependencies, @NotNull JavaVersionRange versionRange, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull String md5) {
		this(repo, dependencies, versionRange, groupId, artifactId, version, md5, groupId);
	}
	
	private CommonDependency(@NotNull Repository repo, List<CommonDependency> dependencies, @NotNull JavaVersionRange versionRange, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull String md5, @NotNull String... groupPaths) {
		
		final String[] kyoriRelocations = { "net,kyori,adventure", "net,kyori,examination" }; // a bit like memory waste, but enums don't like static fields used in constructors
		
		groupId = groupId.replace(',', '.');
		for (int i = 0; i < groupPaths.length; i++) {
			if ("KYORI_RELOCATIONS".equals(groupPaths[i])) {
				String[] newArray = new String[groupPaths.length + kyoriRelocations.length - 1];
				int j = 0;
				while (j < i) newArray[j] = groupPaths[j++];
				while (j < groupPaths.length-1) newArray[j] = groupPaths[++j];
				for (String path : kyoriRelocations) newArray[j++] = path;
				groupPaths = newArray;
			}
		}
		this.dependencies = Collections.unmodifiableList(dependencies);
		this.downloadPath = repo.getUrl() + '/' + groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/';
		this.repository = repo;
		this.versionRange = versionRange;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.md5 = md5;
		this.groupPaths = Stream.of(groupPaths).map(path -> path.replace(',', '.')).toArray(String[]::new);
	}
	
	
	public String getFileName() {
		return this.artifactId + ".jar";
	}
	
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public enum Repository {
		
		
		MAVEN_CENTRAL("https://repo1.maven.org/maven2"),
		SONATYPE_SNAPSHOTS("https://oss.sonatype.org/content/repositories/snapshots"),
		JITPACK("https://jitpack.io"),
		;
		
		
		@Getter private final String url;
		
	}
	
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class JavaVersionRange {
		
		public static final JavaVersionRange ALL = new JavaVersionRange(Float.MIN_VALUE, Float.MAX_VALUE);
		public static final float JAVA_VERSION = Float.parseFloat(System.getProperty("java.class.version"));
	
		@Getter private final float minVersion;
		@Getter private final float maxVersion;
		
		public boolean isApplicable() {
			return this.maxVersion >= JAVA_VERSION && this.minVersion <= JAVA_VERSION;
		}
		
	}
	

}
