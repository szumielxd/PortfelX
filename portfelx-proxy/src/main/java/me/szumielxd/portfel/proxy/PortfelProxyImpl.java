package me.szumielxd.portfel.proxy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.common.loader.CommonDependency;
import me.szumielxd.portfel.common.managers.PrizesManager;
import me.szumielxd.portfel.proxy.api.PortfelProxy;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger;
import me.szumielxd.portfel.proxy.database.hikari.H2DB;
import me.szumielxd.portfel.proxy.database.hikari.MariaDB;
import me.szumielxd.portfel.proxy.database.hikari.MysqlDB;
import me.szumielxd.portfel.proxy.database.hikari.logging.HikariDBLogger;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.database.token.hikari.H2TokenDB;
import me.szumielxd.portfel.proxy.database.token.hikari.MariaTokenDB;
import me.szumielxd.portfel.proxy.database.token.hikari.MysqlTokenDB;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.TokenManager;

public interface PortfelProxyImpl extends PortfelProxy {
	
	public void addToRuntime(CommonDependency... dependency);
	
	public @NotNull String getName();
	
	public @NotNull String getVersion();
	
	public @NotNull String getDescriptionText();
	
	public @NotNull String getAuthor();
	
	public @NotNull AbstractDB getDatabase();
	
	public void setDatabase(@NotNull AbstractDB database);
	
	
	public @NotNull AbstractTokenDB getTokenDatabase();

	public void setTokenDatabase(@NotNull AbstractTokenDB database);
	
	public @NotNull AccessManagerImpl getAccessManager();
	
	
	public @NotNull TokenManager getTokenManager();


	@Override
	public @NotNull UserManager getUserManager();
	
	
	@Override
	public @NotNull ProxyTopManager getTopManager();
	
	
	@Override
	public @NotNull TaskManager getTaskManager();
	
	public @NotNull OrdersManager getOrdersManager();
	
	public @NotNull PrizesManager getPrizesManager();
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	public @NotNull AbstractDBLogger getTransactionLogger();
	
	public void setTransactionLogger(@NotNull AbstractDBLogger databaseLogger);
	
	
	public void load();
	
	public void unload();
	
	public void onEnable();
	
	public void onDisable();
	
	public void setProxyId(UUID proxyId);
	
	public default void setupProxyId() {
		final Path file = this.getDataFolder().resolve("server-id.dat");
		if (Files.exists(file)) {
			try {
				this.setProxyId(UUID.fromString(String.join("\n", Files.readAllLines(file))));
				return;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
				Path to = file.getParent().resolve(file.getFileName() + ".broken");
				try {
					Files.deleteIfExists(to);
					Files.move(file, to);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		try {
			if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
			this.setProxyId(UUID.randomUUID());
			Files.write(file, this.getProxyId().toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public default void setupDatabases() {
		String dbType = this.getConfiguration().getString(ProxyConfigKey.DATABASE_TYPE).toLowerCase();
		if ("mariadb".equals(dbType)) this.setDatabase(new MariaDB(this));
		else if ("mysql".equals(dbType)) this.setDatabase(new MysqlDB(this));
		else this.setDatabase(new H2DB(this));
		this.getLogger().info("Establishing connection with database...");
		this.getDatabase().setup();
		//
		String tokenDbType = this.getConfiguration().getString(ProxyConfigKey.TOKEN_DATABASE_TYPE).toLowerCase();
		if ("mariadb".equals(tokenDbType)) this.setTokenDatabase(new MariaTokenDB(this));
		else if ("mysql".equals(tokenDbType)) this.setTokenDatabase(new MysqlTokenDB(this));
		else this.setTokenDatabase(new H2TokenDB(this));
		this.getLogger().info("Establishing connection with tokens database...");
		this.getTokenDatabase().setup();
		this.setTransactionLogger(new HikariDBLogger(this).init());
	}
	

}
