package me.szumielxd.portfel.proxy.database.hikari;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;

import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class MariaDB<C> extends HikariDB<C> {

	public MariaDB(PortfelProxyImpl<C> plugin) {
		super(plugin);
	}
	
	/**
	 * Get name of database's type
	 * 
	 * @return database type's name
	 */
	@Override
	public @NotNull String getDBName() {
		return "MariaDB";
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
		String dataSource = "me.szumielxd.portfel.lib.org.mariadb.jdbc.MariaDbDataSource";
		try {
			Class.forName(dataSource);
		} catch (ClassNotFoundException e) {
			this.plugin.addToRuntime(CommonDependency.MARIADB);
		}
		config.setDataSourceClassName(dataSource);
		config.addDataSourceProperty("serverName", address);
		config.addDataSourceProperty("port", port);
		config.addDataSourceProperty("databaseName", database);
		config.setUsername(user);
		config.setPassword(password);
	}

}
