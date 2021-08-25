package me.szumielxd.portfel.bungee.database.token.hikari;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.szumielxd.portfel.api.Config;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.configuration.BungeeConfigKey;
import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.database.token.AbstractTokenDB;
import me.szumielxd.portfel.bungee.objects.PrizeToken;
import me.szumielxd.portfel.bungee.objects.PrizeToken.ServerSelectorType;

public abstract class HikariTokenDB implements AbstractTokenDB {
	
	
	protected final PortfelBungeeImpl plugin;
	protected HikariDataSource hikari;
	
	private boolean tablesChecked = false;
	
	
	private final String TABLE_TOKENS;
	
	private final String TOKENS_TOKEN;
	private final String TOKENS_SERVERS;
	private final String TOKENS_ORDERNAME;
	private final String TOKENS_CREATORNAME;
	private final String TOKENS_CREATORUUID;
	private final String TOKENS_CREATIONDATE;
	private final String TOKENS_EXPIRATIONDATE;
	
	private final String DB_HOST;
	private final String DB_NAME;
	private final String DB_USER;
	private final String DB_PASSWD;

	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	
	public HikariTokenDB(PortfelBungeeImpl plugin) {
		this.plugin = plugin;
		Config cfg = this.plugin.getConfiguration();
		
		DB_HOST = cfg.getString(BungeeConfigKey.TOKEN_DATABASE_HOST);
		DB_NAME = cfg.getString(BungeeConfigKey.TOKEN_DATABASE_DATABASE);
		DB_USER = cfg.getString(BungeeConfigKey.TOKEN_DATABASE_USERNAME);
		DB_PASSWD = cfg.getString(BungeeConfigKey.TOKEN_DATABASE_PASSWORD);
		
		TABLE_TOKENS = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_NAME));
		
		TOKENS_TOKEN = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_TOKEN));
		TOKENS_SERVERS = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_SERVERS));
		TOKENS_ORDERNAME = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_ORDERNAME));
		TOKENS_CREATORNAME = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATORNAME));
		TOKENS_CREATORUUID = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATORUUID));
		TOKENS_CREATIONDATE = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_CREATIONDATE));
		TOKENS_EXPIRATIONDATE = escapeSql(cfg.getString(BungeeConfigKey.TOKEN_DATABASE_TABLE_TOKENS_COLLUMN_EXPIRATIONDATE));
		
	}
	
	
	/**
	 * Get default port for this implementation of HikariCP.
	 * 
	 * @return default port
	 */
	protected abstract int getDefaultPort();
	
	
	/**
	 * Setup database connection properties.
	 */
	public void setup() {
		HikariConfig config = new HikariConfig();
		config.setPoolName("portfel-hikari");
		final String[] host = DB_HOST.split(":");
		int port = this.getDefaultPort();
		if (host.length > 1) {
			try {
				port = Integer.parseInt(host[1]);
			} catch (NumberFormatException e) {}
		}
		this.setupDatabase(config, host[0], port, DB_NAME, DB_USER, DB_PASSWD);
		
		Config cfg = this.plugin.getConfiguration();
		Map<String, String> properties = cfg.getStringMap(BungeeConfigKey.TOKEN_DATABASE_POOL_PROPERTIES);
		this.setupProperties(config, properties);
		
		config.setMaximumPoolSize(cfg.getInt(BungeeConfigKey.TOKEN_DATABASE_POOL_MAXSIZE));
		config.setMinimumIdle(cfg.getInt(BungeeConfigKey.TOKEN_DATABASE_POOL_MINIDLE));
		config.setMaxLifetime(cfg.getInt(BungeeConfigKey.TOKEN_DATABASE_POOL_MAXLIFETIME));
		config.setKeepaliveTime(cfg.getInt(BungeeConfigKey.TOKEN_DATABASE_POOL_KEEPALIVE));
		config.setConnectionTimeout(cfg.getInt(BungeeConfigKey.TOKEN_DATABASE_POOL_TIMEOUT));
		config.setInitializationFailTimeout(-1);
		
		this.hikari = new HikariDataSource(config);
		
		if (!this.tablesChecked) {
			try {
				this.setupTables();
				this.cleanupExpired();
				this.tablesChecked = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Modify and setup connection properties.
	 * 
	 * @param properties default properties map
	 */
	protected abstract void setupProperties(@NotNull HikariConfig config, @NotNull Map<String, String> properties);
	
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
	public abstract void setupDatabase(@NotNull HikariConfig config, @NotNull String address, int port, @NotNull String database, @NotNull String user, @NotNull String password);
	
	/**
	 * Get database connection.
	 * 
	 * @return database connection
	 * @throws SQLException when cannot establish database connection
	 */
	@Override
	public Connection connect() throws SQLException {
		if (this.hikari == null) throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
		Connection conn = this.hikari.getConnection();
		if (conn == null) throw new SQLException("Unable to get a connection from the pool. (connection is null)");
		return conn;
	}
	
	/**
	 * Check if database is connected.
	 * 
	 * @return true if connection to database is opened
	 */
	@Override
	public boolean isConnected() {
		return this.isValid() && !this.hikari.isClosed();
	}
	
	/**
	 * Check if database connection is valid.
	 * 
	 * @return true if connection to database is valid
	 */
	@Override
	public boolean isValid() {
		return this.hikari != null;
	}
	
	/**
	 * Shutdown database
	 */
	@Override
	public void shutdown() {
		if (this.hikari != null) this.hikari.close();
	}
	
	/**
	 * Load prize related to given token.
	 * 
	 * @implNote Thread unsafe.
	 * @param  token string unique token
	 * @return prize assigned to this token
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public @Nullable PrizeToken getToken(@NotNull String token) throws SQLException {
		this.checkConnection();
		String sql = String.format("SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ? AND (`%s` = -1 OR `%s` >= UNIX_TIMESTAMP())",
				TOKENS_TOKEN, TOKENS_SERVERS, TOKENS_ORDERNAME, TOKENS_CREATORNAME, TOKENS_CREATORUUID, TOKENS_CREATIONDATE, TOKENS_EXPIRATIONDATE,
				TABLE_TOKENS, TOKENS_TOKEN, TOKENS_EXPIRATIONDATE, TOKENS_EXPIRATIONDATE);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, token);
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						if (!Objects.equal(token, rs.getString(1))) return null;
						String order = rs.getString(3);
						ActionExecutor creator = new ActionExecutor(rs.getString(4), UUID.fromString(rs.getString(5))) {};
						Date creationDate = rs.getDate(6);
						long expiration = rs.getLong(7);
						String selector = rs.getString(2);
						Set<String> servers = new HashSet<>();
						ServerSelectorType type;
						if ("*".equals(selector)) {
							type = ServerSelectorType.ANY;
						} else if ("+".equals(selector)) {
							type = ServerSelectorType.REGISTERED;
						} else {
							type = ServerSelectorType.WHITELIST;
							servers = new HashSet<>(Arrays.asList(selector.split(",")));
						}
						return new PrizeToken(token, servers, type, order, creator, creationDate, expiration);
					}
					return null;
				}
			}
		}
	}
	
	/**
	 * Remove token from database. Used to set token as done.
	 * 
	 * @implNote Thread unsafe.
	 * @param token string token
	 * @return true if token was removed from database, otherwise false
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public boolean destroyToken(@NotNull String token) throws SQLException {
		this.checkConnection();
		String sql = String.format("DELETE FROM `%s` WHERE `%s` = ?", TABLE_TOKENS, TOKENS_TOKEN);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, token);
				return stm.executeUpdate() > 0;
			}
		}
	}
	
	/**
	 * Register new prize with unique token.
	 * 
	 * @param token unique string token
	 * @param servers list of servers when this token can be executed
	 * @param order order to execute on success token match
	 * @param creator creator of this prize
	 * @param expiration date timestamp when token will expire, set to -1 for non-expiring token
	 * @throws SQLException when cannot establish the connection to the database or token already exists
	 */
	@Override
	public void registerToken(@NotNull String token, @NotNull String servers, @NotNull String order, @NotNull ActionExecutor creator, long expiration) throws SQLException {
		this.checkConnection();
		String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?, ?)",
				TABLE_TOKENS, TOKENS_TOKEN, TOKENS_SERVERS, TOKENS_ORDERNAME, TOKENS_CREATORNAME, TOKENS_CREATORUUID, TOKENS_EXPIRATIONDATE);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, token);
				stm.setString(2, servers);
				stm.setString(3, order);
				stm.setString(4, creator.getDisplayName());
				stm.setString(5, creator.getUniqueId().toString());
				stm.setLong(6, expiration);
				stm.executeUpdate();
			}
		}
	}
	
	/**
	 * Get all active gift-codes.
	 * 
	 * @param servers servers where token can be used
	 * @param orders list of filtered orders
	 * @param creators list of creators to filter
	 * @param creationDateConditions creation date filter
	 * @param expirationDateConditions expiration date filter. Type -1 for non-expiring
	 * @return list of valid prize tokens
	 * @throws when cannot establish the connection to the database or token already exists
	 */
	@Override
	public @NotNull List<PrizeToken> getTokens(@Nullable String[] servers, @Nullable String[] orders, @Nullable String[] creators, @Nullable DateCondition[] creationDateConditions, @Nullable DateCondition[] expirationDateConditions) throws SQLException {
		this.checkConnection();
		
		// parse target and executor
		List<String> creatorNames = new ArrayList<>();
		List<UUID> creatorIds = new ArrayList<>();
		if (creators != null) Stream.of(creators).forEach(str -> {
			try {creatorIds.add(UUID.fromString(str));} catch (IllegalArgumentException e) {creatorNames.add(str);}
		});
		
		// create builder
		StringBuilder whereClause = new StringBuilder();
		
		// creators
		if (!creatorNames.isEmpty()) whereClause.append(" AND `").append(TOKENS_CREATORNAME).append('`').append(" IN (").append(String.join(", ", creatorNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		if (!creatorIds.isEmpty()) whereClause.append(" AND `").append(TOKENS_CREATORUUID).append('`').append(" IN (").append(String.join(", ", creatorNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		// servers
		if (servers != null && servers.length > 0) whereClause.append(" AND (").append(String.join(" OR ", Stream.of(servers).map(s -> String.format("`%s` LIKE ?", TOKENS_SERVERS)).toArray(String[]::new))).append(')');
		// orders
		if (orders != null && orders.length > 0) whereClause.append(" AND (").append(String.join(" OR ", Stream.of(orders).map(s -> String.format("`%s` LIKE ?", TOKENS_ORDERNAME)).toArray(String[]::new))).append(')');
		// creation date
		if (creationDateConditions != null && creationDateConditions.length > 0) {
			whereClause.append(" AND (").append(String.join(" AND ", Stream.of(creationDateConditions).map(c -> String.format("`%s` %s", TOKENS_CREATIONDATE, c.getFormat())).toArray(String[]::new))).append(')');
		}
		// expiration date
		if (expirationDateConditions != null && expirationDateConditions.length > 0) {
			whereClause.append(" AND (").append(String.join(" AND ", Stream.of(expirationDateConditions).map(c -> String.format("`%s` %s", TOKENS_EXPIRATIONDATE, c.getFormat())).toArray(String[]::new))).append(')');
		}
		
		AbstractDB db = this.plugin.getDB();
		db.checkConnection();
		String sql = String.format("SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE (`%s` = -1 OR `%s` >= UNIX_TIMESTAMP()) ",
				TOKENS_TOKEN, TOKENS_SERVERS, TOKENS_ORDERNAME, TOKENS_CREATORNAME, TOKENS_CREATORUUID,
				TOKENS_CREATIONDATE, TOKENS_EXPIRATIONDATE, TABLE_TOKENS, TOKENS_EXPIRATIONDATE, TOKENS_EXPIRATIONDATE);
		if (whereClause.length() > 0) sql = whereClause.insert(0, sql).append(String.format(" ORDER BY `%s` ASC", TOKENS_CREATIONDATE)).toString();
		
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				int i = 0;
				// creators
				if (!creatorNames.isEmpty()) for (String s : creatorNames) stm.setString(++i, s);
				if (!creatorIds.isEmpty()) for (UUID s : creatorIds) stm.setString(++i, s.toString());
				// servers
				if (servers != null && servers.length > 0) for (String s : servers) stm.setString(++i, this.likeContains(s));
				// orders
				if (orders != null && orders.length > 0) for (String s : orders) stm.setString(++i, this.likeContains(s));
				// value
				if (creationDateConditions != null && creationDateConditions.length > 0) for (DateCondition c : creationDateConditions) for (long val : c.getValues()) stm.setLong(++i, val);
				// balance
				if (expirationDateConditions != null && expirationDateConditions.length > 0) for (DateCondition c : expirationDateConditions) for (long val : c.getValues()) stm.setLong(++i, val);
				
				try (ResultSet rs = stm.executeQuery()) {
					List<PrizeToken> list = new ArrayList<>(rs.getFetchSize());
					while (rs.next()) {
						ActionExecutor creator = new ActionExecutor(rs.getString(4), UUID.fromString(rs.getString(5))) {};
						Date creationDate = rs.getDate(6);
						long expiration = rs.getLong(7);
						String selector = rs.getString(2);
						Set<String> serversSet = new HashSet<>();
						ServerSelectorType type;
						if ("*".equals(selector)) {
							type = ServerSelectorType.ANY;
						} else if ("+".equals(selector)) {
							type = ServerSelectorType.REGISTERED;
						} else {
							type = ServerSelectorType.WHITELIST;
							serversSet = new HashSet<>(Arrays.asList(selector.split(",")));
						}
						list.add(new PrizeToken(rs.getString(1), serversSet, type, rs.getString(3), creator, creationDate, expiration));
					}
					return list;
				}
			}
		}
	}
	
	/**
	 * Remove expired tokens from database.
	 * 
	 * @implNote Thread unsafe.
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void cleanupExpired() throws SQLException {
		this.checkConnection();
		String sql = String.format("DELETE FROM `%s` WHERE `%s` <> -1 AND `%s` < UNIX_TIMESTAMP()", TABLE_TOKENS, TOKENS_EXPIRATIONDATE, TOKENS_EXPIRATIONDATE);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.executeUpdate();
			}
			
		}
	}
	
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection() {
		if (!this.isConnected()) this.setup();
	}
	
	/**
	 * Check for tables existence and create them if not exists already.
	 * 
	 * @throws SQLException when cannot establish the connection to the database
	 */
	private void setupTables() throws SQLException {
		this.checkConnection();
		String tokensTable = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` VARCHAR(32) BINARY NOT NULL, `%s` TEXT NOT NULL, `%s` VARCHAR(32) NOT NULL,"
				+ "`%s` VARCHAR(16) NOT NULL, `%s` VARCHAR(36) NOT NULL, `%s` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP , `%s` BIGINT NOT NULL DEFAULT '-1' , PRIMARY KEY (`%s`)) ENGINE = InnoDB CHARSET=ascii COLLATE ascii_general_ci;",
				TABLE_TOKENS, TOKENS_TOKEN, TOKENS_SERVERS, TOKENS_ORDERNAME, TOKENS_CREATORNAME, TOKENS_CREATORUUID, TOKENS_CREATIONDATE, TOKENS_EXPIRATIONDATE, TOKENS_TOKEN);
		try (Connection conn = this.hikari.getConnection()) {
			try (Statement stm = conn.createStatement()) {
				stm.execute(tokensTable);
			}
		}
	}

	private @NotNull String escapeLikeWildcards(@NotNull String text) {
		return text.replace("%", "\\%").replace("_", "\\_");
	}
	
	private @NotNull String likeContains(@NotNull String text) {
		return "%" + this.escapeLikeWildcards(text) + "%";
	}

}
