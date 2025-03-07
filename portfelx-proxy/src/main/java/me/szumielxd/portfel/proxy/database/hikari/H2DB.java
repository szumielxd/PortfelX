package me.szumielxd.portfel.proxy.database.hikari;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;

import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class H2DB<C> extends HikariDB<C> {
	
	
	public static final Map<Pattern, String> mapping = Map.of(
			Pattern.compile("VARCHAR([^ ]*) BINARY", Pattern.CASE_INSENSITIVE), "VARCHAR_CASESENSITIVE$1",
			Pattern.compile(" CHARSET=[^ ;]+", Pattern.CASE_INSENSITIVE), "",
			Pattern.compile(" COLLATE [^ ;]+", Pattern.CASE_INSENSITIVE), "",
			Pattern.compile("(?<= )LIKE(?= )", Pattern.CASE_INSENSITIVE), "ILIKE",
			Pattern.compile(" UNSIGNED(?= )", Pattern.CASE_INSENSITIVE), "",
			Pattern.compile(" CHARACTER SET [^ ;]+", Pattern.CASE_INSENSITIVE), "",
			Pattern.compile(" ENGINE = [^ ;]+", Pattern.CASE_INSENSITIVE), "",
			Pattern.compile("UNIX_TIMESTAMP\\(\\)"), "DATEDIFF\\('SECOND', DATE '1970-01-01', CURRENT_TIMESTAMP\\(\\)\\) * 1000",
			Pattern.compile("^ALTER TABLE ([^ ]+) ADD INDEX ([^ ]+)\\(([^ ]+)\\)$", Pattern.CASE_INSENSITIVE), "CREATE INDEX $2 ON $1 \\($3\\)");
	

	public H2DB(PortfelProxyImpl<C> plugin) {
		super(plugin);
	}
	
	
	/**
	 * Applies additional mappings depending on database type.
	 * 
	 * @param query to process
	 * @return given query with applied mappings
	 */
	@Override
	protected @NotNull String mapQuery(@NotNull String query) {
		for (var entry : mapping.entrySet()) {
			query = entry.getKey().matcher(query).replaceAll(entry.getValue());
		}
		plugin.debug("[QUERY] \u001b[36m%s\u001b[0m", query);
		if (!query.endsWith(";")) {
			query += ";";
		}
		return query;
	}
	
	/**
	 * Get name of database's type
	 * 
	 * @return database type's name
	 */
	@Override
	public @NotNull String getDBName() {
		return "H2";
	}

	/**
	 * Get default port for this implementation of HikariCP.
	 * 
	 * @return default port
	 */
	@Override
	protected int getDefaultPort() {
		return -1;
	}

	/**
	 * Modify and setup connection properties.
	 * 
	 * @param properties default properties map
	 */
	@Override
	protected void setupProperties(@NotNull HikariConfig config, @NotNull Map<String, String> properties) {
		properties.putIfAbsent("loginTimeout", "30000");
		//properties.forEach((k,v) -> config.addDataSourceProperty(k, v));
	}

	/**
	 * Setup database connection.
	 * 
	 * @param config database configuration object
	 * @param address connection's address
	 * @param port connection's port
	 * @param database database name
	 * @param user database user name
	 * @param password database password
	 */
	@Override
	public void setupDatabase(@NotNull HikariConfig config, @NotNull String address, int port, @NotNull String database, @NotNull String user, @NotNull String password) {
		Path file = this.plugin.getDataDirectory().resolve(this.plugin.getName().toLowerCase() + "-h2").toAbsolutePath();
		String dataSource = "me.szumielxd.portfel.lib.org.h2.jdbcx.JdbcDataSource";
		config.setDataSourceClassName(dataSource);
		config.addDataSourceProperty("URL", "jdbc:h2:" + file + ";IGNORECASE=TRUE");
		config.setUsername(user);
		config.setPassword(password);
	}
	
	

}
