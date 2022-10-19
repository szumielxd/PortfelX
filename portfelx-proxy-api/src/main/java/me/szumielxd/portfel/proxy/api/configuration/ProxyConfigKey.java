package me.szumielxd.portfel.proxy.api.configuration;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;

import me.szumielxd.portfel.api.configuration.AbstractKey;

public enum ProxyConfigKey implements AbstractKey {
	
	MAIN_TOP_SIZE("main.top-size", 20),
	//
	DATABASE_TYPE("database.type", "H2"),
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
	DATABASE_TABLE_USERS_COLLUMN_MINORBALANCE("database.table.users.collumn.minor-balance", "minor_balance"),
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
	
	TOKEN_COMMAND_NAME("token.command.name", "token"),
	TOKEN_COMMAND_ALIASES("token.command.aliases", Arrays.asList("trytoken", "prize")),
	//
	TOKEN_MANAGER_POOLSIZE("token.manager.pool-size", 10),
	//
	TOKEN_DATABASE_TYPE("token.token-database.type", "H2"),
	//
	TOKEN_DATABASE_HOST("token.token-database.host", "localhost"),
	TOKEN_DATABASE_DATABASE("token.token-database.database", "portfel"),
	TOKEN_DATABASE_USERNAME("token.token-database.username", "root"),
	TOKEN_DATABASE_PASSWORD("token.token-database.password", ""),
	//
	TOKEN_DATABASE_POOL_MAXSIZE("token.token-database.pool-options.maximum-pool-size", 10),
	TOKEN_DATABASE_POOL_MINIDLE("token.token-database.pool-options.minimum-idle", 10),
	TOKEN_DATABASE_POOL_MAXLIFETIME("token.token-database.pool-options.maximum-lifetime", 1800000),
	TOKEN_DATABASE_POOL_TIMEOUT("token.token-database.pool-options.connection-timeout", 5000),
	TOKEN_DATABASE_POOL_KEEPALIVE("token.token-database.pool-options.keep-alive-time", 0),
	TOKEN_DATABASE_POOL_PROPERTIES("token.token-database.pool-options.properties", ImmutableMap.of("useUnicode", "true", "characterEncoding", "utf8")),
	//
	TOKEN_DATABASE_TABLE_TOKENS_NAME("token.token-database.table.tokens.name", "wallet_tokens"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_TOKEN("token.token-database.table.tokens.collumn.token", "token"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_SERVERS("token.token-database.table.tokens.collumn.servers", "servers"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_ORDERNAME("token.token-database.table.tokens.collumn.order", "order_name"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATORNAME("token.token-database.table.tokens.collumn.creator-name", "creator_name"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATORUUID("token.token-database.table.tokens.collumn.creator-uuid", "creator_uuid"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATIONDATE("token.token-database.table.tokens.collumn.creation-date", "creation_date"),
	TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_EXPIRATIONDATE("token.token-database.table.tokens.collumn.expiration-date", "expiration_date"),
	;

	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private ProxyConfigKey(@NotNull String path, @NotNull Object defaultValue) {
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
