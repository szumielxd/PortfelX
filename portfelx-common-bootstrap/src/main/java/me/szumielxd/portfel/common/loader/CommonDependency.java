package me.szumielxd.portfel.common.loader;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public enum CommonDependency {
	
	
	MARIADB(Repository.MAVEN_CENTRAL, JavaVersionRange.ALL, "org.mariadb.jdbc", "mariadb-java-client", "2.7.4", "9ca3eac191f81a8c0062e35de6e35ed8"),
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
	;
	
	
	private final String[] KYORI_RELOCATIONS = { "net,kyori,adventure", "net,kyori,examination", "net,kyori,adventure,platform", "net,kyori,adventure,util" }; // it hurts!
	
	
	@Getter private final String downloadPath;
	@Getter private final Repository repository;
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
		groupId = groupId.replace(',', '.');
		for (int i = 0; i < groupPaths.length; i++) {
			if ("KYORI_RELOCATIONS".equals(groupPaths[i])) {
				String[] newArray = new String[groupPaths.length + KYORI_RELOCATIONS.length - 1];
				int j = 0;
				while (j < i) newArray[j] = groupPaths[j++];
				while (j < groupPaths.length-1) newArray[j] = groupPaths[++j];
				for (String path : KYORI_RELOCATIONS) newArray[j++] = path;
				groupPaths = newArray;
			}
		}
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
