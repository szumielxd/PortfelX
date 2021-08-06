package me.szumielxd.portfel.bungee;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;

import me.szumielxd.portfel.common.Config.AbstractKey;

public enum BungeeConfigKey implements AbstractKey {
	
	DATABASE_TYPE("database.type", "MySQL"),
	//
	DATABASE_HOST("database.host", "localhost"),
	DATABASE_DATABASE("database.database", "portfel"),
	DATABASE_USERNAME("database.username", "root"),
	DATABASE_PASSWORD("database.password", ""),
	//
	DATABASE_POOL_MAXSIZE("database.pool-options.maximum-pool-size", 10),
	DATABASE_POOL_MINIDLE("database.pool-options.minimum-idle", 10),
	DATABASE_POOL_MAXLIFETIME("database.pool-options.maximum-lifetime", 1800000),
	DATABASE_POOL_TIMEOUT("database.pool-options.connection-timeout", 5000),
	DATABASE_POOL_KEEPALIVE("database.pool-options.keep-alive-time", 0),
	DATABASE_POOL_PROPERTIES("database.pool-options.properties", ImmutableMap.of("useUnicode", "true", "characterEncoding", "utf8")),
	//
	DATABASE_TABLE_USERS_NAME("database.table.users.name", "wallet_users"),
	DATABASE_TABLE_USERS_COLLUMN_USERNAME("database.table.users.collumn.username", "username"),
	DATABASE_TABLE_USERS_COLLUMN_UUID("database.table.users.collumn.uuid", "uuid"),
	DATABASE_TABLE_USERS_COLLUMN_BALANCE("database.table.users.collumn.balance", "balance"),
	DATABASE_TABLE_USERS_COLLUMN_IGNORETOP("database.table.users.collumn.intop", "ignore_top"),
	//
	DATABASE_TABLE_LOGS_NAME("database.table.logs.name", "wallet_logs"),
	DATABASE_TABLE_LOGS_COLLUMN_ID("database.table.logs.collumn.id", "id"),
	DATABASE_TABLE_LOGS_COLLUMN_UUID("database.table.logs.collumn.uuid", "uuid"),
	DATABASE_TABLE_LOGS_COLLUMN_USERNAME("database.table.logs.collumn.username", "username"),
	DATABASE_TABLE_LOGS_COLLUMN_SERVER("database.table.logs.collumn.server", "server"),
	DATABASE_TABLE_LOGS_COLLUMN_EXECUTOR("database.table.logs.collumn.executor", "executor"),
	DATABASE_TABLE_LOGS_COLLUMN_EXECUTORUUID("database.table.logs.collumn.executor-uuid", "executor_uuid"),
	DATABASE_TABLE_LOGS_COLLUMN_TIME("database.table.logs.collumn.time", "time"),
	DATABASE_TABLE_LOGS_COLLUMN_ORDERNAME("database.table.logs.collumn.order-name", "order_name"),
	DATABASE_TABLE_LOGS_COLLUMN_ACTION("database.table.logs.collumn.action", "action"),
	DATABASE_TABLE_LOGS_COLLUMN_VALUE("database.table.logs.collumn.value", "value"),
	DATABASE_TABLE_LOGS_COLLUMN_BALANCE("database.table.logs.collumn.balance", "balance"),
	;

	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private BungeeConfigKey(@NotNull String path, @NotNull Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.type = defaultValue.getClass();
	}
	
	public String getPath() {
		return this.path;
	}
	
	public Object getDefault() {
		return this.defaultValue;
	}
	
	public Class<?> getType() {
		return this.type;
	}

}
