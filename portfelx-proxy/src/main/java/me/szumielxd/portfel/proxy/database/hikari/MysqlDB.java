package me.szumielxd.portfel.proxy.database.hikari;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class MysqlDB extends HikariDB {

	public MysqlDB(PortfelProxyImpl plugin) {
		super(plugin);
	}
	
	/**
	 * Get name of database's type
	 * 
	 * @return database type's name
	 */
	@Override
	public @NotNull String getDBName() {
		return "MySQL";
	}

	/**
	 * Get default port for this implementation of HikariCP.
	 * 
	 * @return default port
	 */
	@Override
	protected int getDefaultPort() {
		return 3306;
	}

	/**
	 * Modify and setup connection properties.
	 * 
	 * @param properties default properties map
	 */
	@Override
	protected void setupProperties(@NotNull HikariConfig config, @NotNull Map<String, String> properties) {
		properties.putIfAbsent("socketTimeout", "30000");
		properties.putIfAbsent("cachePrepStmts", "true");
		properties.putIfAbsent("prepStmtCacheSize", "250");
		properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
		properties.putIfAbsent("useServerPrepStmts", "true");
		properties.putIfAbsent("useLocalSessionState", "true");
		properties.putIfAbsent("rewriteBatchedStatements", "true");
		properties.putIfAbsent("cacheResultSetMetadata", "true");
		properties.putIfAbsent("cacheServerConfiguration", "true");
		properties.putIfAbsent("elideSetAutoCommits", "true");
		properties.putIfAbsent("maintainTimeStats", "false");
		properties.putIfAbsent("alwaysSendSetIsolation", "false");
		properties.putIfAbsent("cacheCallableStmts", "true");
		properties.putIfAbsent("serverTimezone", "UTC");
		properties.forEach((k,v) -> config.addDataSourceProperty(k, v));
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
		String driver = "me.szumielxd.portfel.lib.com.mysql.cj.jdbc.Driver";
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			this.plugin.addToRuntime(CommonDependency.MYSQL);
		}
		config.setDriverClassName(driver);
		config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + database);
		config.setUsername(user);
		config.setPassword(password);
	}

}
