package me.szumielxd.portfel.proxy.database.token.hikari;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.database.hikari.H2DB;

public class H2TokenDB extends HikariTokenDB {

	
	public H2TokenDB(PortfelProxyImpl plugin) {
		super(plugin);
	}
	
	
	/**
	 * Applies additional mappings depending on database type.
	 * 
	 * @param query to process
	 * @return given query with applied mappings
	 */
	protected @NotNull String mapQuery(@NotNull String query) {
		Objects.requireNonNull(query, "query cannot be null");
		for (Map.Entry<Pattern, String> entry : H2DB.MAPPING.entrySet()) {
			query = entry.getKey().matcher(query).replaceAll(entry.getValue());
		}
		this.plugin.debug("[QUERY] \u001b[36m%s\u001b[0m", query);
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
		File file = new File(this.plugin.getDataFolder(), this.plugin.getName().toLowerCase() + "-tokens-h2");
		String dataSource = "me.szumielxd.portfel.lib.org.h2.jdbcx.JdbcDataSource";
		try {
			Class.forName(dataSource);
		} catch (ClassNotFoundException e) {
			this.plugin.addToRuntime(CommonDependency.H2);
		}
		config.setDataSourceClassName(dataSource);
		config.addDataSourceProperty("URL", "jdbc:h2:" + file.getAbsolutePath() + ";IGNORECASE=TRUE");
		config.setUsername(user);
		config.setPassword(password);
	}

}
