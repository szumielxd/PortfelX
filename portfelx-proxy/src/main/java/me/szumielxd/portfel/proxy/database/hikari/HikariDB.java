package me.szumielxd.portfel.proxy.database.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.szumielxd.portfel.api.configuration.Config;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public abstract class HikariDB implements AbstractDB {
	
	
	protected final PortfelProxyImpl plugin;
	protected HikariDataSource hikari;
	
	private boolean tablesChecked = false;
	
	
	private final String TABLE_USERS;
	private final String TABLE_LOGS;
	
	private final String USERS_NAME;
	private final String USERS_UUID;
	private final String USERS_BALANCE;
	private final String USERS_IGNORETOP;
	
	private final String LOGS_ID;
	private final String LOGS_UUID;
	private final String LOGS_USERNAME;
	private final String LOGS_SERVER;
	private final String LOGS_EXECUTOR;
	private final String LOGS_EXECUTORUUID;
	private final String LOGS_TIME;
	private final String LOGS_ORDERNAME;
	private final String LOGS_ACTION;
	private final String LOGS_VALUE;
	private final String LOGS_BALANCE;
	
	private final String DB_HOST;
	private final String DB_NAME;
	private final String DB_USER;
	private final String DB_PASSWD;

	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	
	public HikariDB(PortfelProxyImpl plugin) {
		this.plugin = plugin;
		Config cfg = this.plugin.getConfiguration();
		
		DB_HOST = cfg.getString(ProxyConfigKey.DATABASE_HOST);
		DB_NAME = cfg.getString(ProxyConfigKey.DATABASE_DATABASE);
		DB_USER = cfg.getString(ProxyConfigKey.DATABASE_USERNAME);
		DB_PASSWD = cfg.getString(ProxyConfigKey.DATABASE_PASSWORD);
		
		TABLE_USERS = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_NAME));
		TABLE_LOGS = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_NAME));
		
		USERS_NAME = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_USERNAME));
		USERS_UUID = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_UUID));
		USERS_BALANCE = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_BALANCE));
		USERS_IGNORETOP = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_IGNORETOP));
		
		LOGS_ID = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ID));
		LOGS_UUID = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_UUID));
		LOGS_USERNAME = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_USERNAME));
		LOGS_SERVER = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_SERVER));
		LOGS_EXECUTOR = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_EXECUTOR));
		LOGS_EXECUTORUUID = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_EXECUTORUUID));
		LOGS_TIME = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_TIME));
		LOGS_ORDERNAME = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ORDERNAME));
		LOGS_ACTION = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ACTION));
		LOGS_VALUE = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_VALUE));
		LOGS_BALANCE = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_COLLUMN_BALANCE));
		
	}
	
	
	/**
	 * Applies additional mappings depending on database type.
	 * 
	 * @param query to process
	 * @return given query with applied mappings
	 */
	protected @NotNull String mapQuery(@NotNull String query) {
		return Objects.requireNonNull(query, "query cannot be null");
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
		Map<String, String> properties = cfg.getStringMap(ProxyConfigKey.DATABASE_POOL_PROPERTIES);
		this.setupProperties(config, properties);
		
		config.setMaximumPoolSize(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MAXSIZE));
		config.setMinimumIdle(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MINIDLE));
		config.setMaxLifetime(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MAXLIFETIME));
		config.setKeepaliveTime(cfg.getInt(ProxyConfigKey.DATABASE_POOL_KEEPALIVE));
		config.setConnectionTimeout(cfg.getInt(ProxyConfigKey.DATABASE_POOL_TIMEOUT));
		config.setInitializationFailTimeout(-1);
		
		this.hikari = new HikariDataSource(config);
		
		if (!this.tablesChecked) {
			try {
				this.setupTables();
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
	 * Load User with given username from database.
	 * 
	 * @implNote Thread unsafe.
	 * @param last known name of user
	 * @param if true query will be case sensitive
	 * @return user related to given name or null when username does not exists in database
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public @Nullable User loadUserByName(@NotNull String name, boolean strict) throws SQLException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` =%s ?;", USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_NAME, (strict ? " BINARY" : "")));
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, name);
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						ProxyPlayer player = this.plugin.getCommonServer().getPlayer(uuid);
						return new ProxyOperableUser(this.plugin, uuid, rs.getString(2), player != null && player.isConnected(), rs.getBoolean(4), rs.getLong(3));
					}
					return null;
				}
			}
		}
	}
	
	/**
	 * Load User with given UUID from database.
	 * 
	 * @implNote Thread unsafe.
	 * @param uuid unique identifier of user
	 * @return user related to given UUID or null when UUID does not exists in database
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public @Nullable User loadUser(@NotNull UUID uuid) throws SQLException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ?;", USERS_NAME, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_UUID));
		ProxyPlayer player = this.plugin.getCommonServer().getPlayer(uuid);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, uuid.toString());
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						return new ProxyOperableUser(this.plugin, uuid, rs.getString(1), player != null && player.isConnected(), rs.getBoolean(3), rs.getLong(2));
					}
				}
			}
			
			// fallback to old username offline-mode system
			if (!MiscUtils.isOnlineModeUUID(uuid) && player != null) {
				sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = BINARY ? AND `%s` IS NULL;", USERS_UUID, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_NAME, USERS_UUID));
				try (PreparedStatement stm = conn.prepareStatement(sql)) {
					stm.setString(1, player.getName());
					try (ResultSet rs = stm.executeQuery()) {
						if (rs.next()) {
							return new ProxyOperableUser(this.plugin, uuid, rs.getString(1), player.isConnected(), rs.getBoolean(3), rs.getLong(2));
						}
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * Load User with given UUID from database or create new one when user does not exists in database.
	 * 
	 * @implNote Thread unsafe.
	 * @param uuid unique identifier of user
	 * @return user related to given UUID
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public @NotNull User loadOrCreateUser(@NotNull UUID uuid) throws SQLException, IllegalStateException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ?;", USERS_NAME, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_UUID));
		ProxyPlayer player = this.plugin.getCommonServer().getPlayer(uuid);
		ProxyOperableUser user = null;
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, uuid.toString());
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						user = new ProxyOperableUser(this.plugin, uuid, rs.getString(1), player != null && player.isConnected(), rs.getBoolean(3), rs.getLong(2));
					}
				}
			}
			
			if (user != null && player != null) {
				if (user.getName() != player.getName()) {
					user.setName(player.getName());
					sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?;", TABLE_USERS, USERS_NAME, USERS_UUID));
					try (PreparedStatement stm = conn.prepareStatement(sql)) {
						stm.setString(1, user.getName());
						stm.setString(2, user.getUniqueId().toString());
					}
				}
				return user;
			}
			
			if (player == null) throw new IllegalStateException("Cannot create new user for offline player");
			
			// fallback to old username offline-mode system
			if (!MiscUtils.isOnlineModeUUID(uuid)) {
				sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = BINARY ? AND `%s` IS NULL;", USERS_UUID, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_NAME, USERS_UUID));
				try (PreparedStatement stm = conn.prepareStatement(sql)) {
					stm.setString(1, player.getName());
					try (ResultSet rs = stm.executeQuery()) {
						if (rs.next()) {
							user = new ProxyOperableUser(this.plugin, uuid, rs.getString(1), player.isConnected(), rs.getBoolean(3), rs.getLong(2));
						}
					}
				}
				if (user != null) {
					sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = BINARY ? AND `%s` IS NULL LIMIT 1;", TABLE_USERS, USERS_UUID, USERS_NAME, USERS_UUID, USERS_NAME, USERS_UUID));
					try (PreparedStatement stm = conn.prepareStatement(sql)) {
						stm.setString(1, player.getName());
						stm.executeQuery();
					}
					return user;
				}
			}
			
			// create new user
			sql = this.mapQuery(String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?);", TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_IGNORETOP));
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				if (user == null) user = new ProxyOperableUser(this.plugin, uuid, player.getName(), player.isConnected(), false, 0);
				stm.setString(1, user.getUniqueId().toString());
				stm.setString(2, user.getName());
				stm.setLong(3, user.getBalance());
				stm.setBoolean(4, user.isDeniedInTop());
				stm.executeUpdate();
				return user;
			}
		}
	}
	
	/**
	 * Get position of given users in balance top. If user doesn't exist in top, then returned position is null.
	 * 
	 * @implNote Thread unsafe.
	 * @param users array of users to get
	 * @return array of positions in the same order as given users array
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public @NotNull Integer[] getTopPos(User... users) throws SQLException {
		Integer[] arr = new Integer[users.length];
		if (users.length == 0) return arr;
		UUID[] uuids = Stream.of(users).map(User::getUniqueId).toArray(UUID[]::new);
		this.checkConnection();
		final String uuidMarks = String.join(", ", Stream.of(uuids).map(s -> "?").toArray(String[]::new));
		final String sql = this.mapQuery(String.format("SELECT CAST(`pos` as INT), `%s` FROM (SELECT (@i:=@i + 1) AS `pos`, `%s` FROM `%s`, (SELECT @i:=0) AS `i` WHERE `%s` = false ORDER BY `%s` DESC) as `top` WHERE `%s` IN (%s);", USERS_UUID, USERS_UUID, TABLE_USERS, USERS_IGNORETOP, USERS_BALANCE, USERS_UUID, uuidMarks));
		try (Connection conn = this.connect(); PreparedStatement stm = conn.prepareStatement(sql)) {
			int index = 0;
			// fill query with UUIDs
			for (; index < uuids.length; index++) {
				stm.setString(index+1, uuids[index].toString());
			}
			try (ResultSet rs = stm.executeQuery()) {
				while (rs.next()) {
					int pos = rs.getInt(1);
					UUID uuid = UUID.fromString(rs.getString(2));
					for (int i = 0; i < uuids.length; i++) {
						if (uuids[i].equals(uuid)) {
							arr[i] = pos;
						}
					}
				}
			}
		}
		return arr;
		
		
		
		
		
	}
	
	/**
	 * Update given user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param users user to update
	 * @return list of all updated users
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public List<ProxyOperableUser> updateUsers(@NotNull ProxyOperableUser... users) throws SQLException {
		List<ProxyOperableUser> updatedUsers = new ArrayList<>();
		if (users.length == 0) return updatedUsers;
		this.checkConnection();
		
		// map users by UUID
		final Map<UUID, ProxyOperableUser> map = Stream.of(users).collect(Collectors.toMap(User::getUniqueId, Function.identity(), (a, b) -> a));
		// generate right amount of `?` characters to insert into query
		final String uuidMarks = String.join(", ", map.keySet().stream().map(s -> "?").toArray(String[]::new));
		final String sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` IN (%s);", USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_UUID, uuidMarks));
		try (Connection conn = this.connect(); PreparedStatement stm = conn.prepareStatement(sql)) {
			int index = 0;
			// fill query with UUIDs
			for (UUID uuid : map.keySet()) {
				stm.setString(++index, uuid.toString());
			}
			try (ResultSet rs = stm.executeQuery()) {
				while (rs.next()) {
					UUID uuid = UUID.fromString(rs.getString(1));
					ProxyOperableUser user = map.get(uuid);
					user.setName(rs.getString(2));
					user.setPlainBalance(rs.getLong(3));
					user.setPlainDeniedInTop(rs.getBoolean(4));
					updatedUsers.add(user);
				}
			}
		}
		return updatedUsers;
	}
	
	/**
	 * Add given amount of money to balance of specified user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to add
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void addBalance(@NotNull ProxyOperableUser user, long amount) throws Exception {
		this.checkConnection();
		String sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = `%s` + ? WHERE `%s` = ?;", TABLE_USERS, USERS_BALANCE, USERS_BALANCE, USERS_UUID));
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
			}
		}
	}
	
	/**
	 * Take given amount of money from balance of specified user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to take
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void takeBalance(@NotNull ProxyOperableUser user, long amount) throws SQLException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = `%s` - ? WHERE `%s` = ?;", TABLE_USERS, USERS_BALANCE, USERS_BALANCE, USERS_UUID));
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
			}
		}
	}
	
	/**
	 * Set balance of specified user to given amount
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param balance new balance
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void setBalance(@NotNull ProxyOperableUser user, long balance) throws SQLException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?;", TABLE_USERS, USERS_BALANCE, USERS_UUID));
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, balance);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
			}
		}
	}
	
	/**
	 * Set whether user should be visible in balance top
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param deniedInTop true if user can be visible in top
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void setDeniedInTop(@NotNull ProxyOperableUser user, boolean deniedInTop) throws SQLException {
		this.checkConnection();
		String sql = this.mapQuery(String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?;", TABLE_USERS, USERS_IGNORETOP, USERS_UUID));
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setBoolean(1, deniedInTop);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) throw new SQLException("Unable to update a user's deniedInTop state. (inexistent uuid)");
			}
		}
	}
	
	/**
	 * Set whether user should be visible in balance top.
	 * 
	 * @implNote Internal use only, try {@link TopManager} instead. Thread unsafe.
	 * @param limit max size of top
	 * @return list of top entries sorted from first to last
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> getTop(int limit) throws SQLException {
		this.checkConnection();
		List<TopEntry> list = new ArrayList<>();
		String sql = this.mapQuery(String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ? ORDER BY `%s` DESC LIMIT ?;", USERS_UUID, USERS_NAME, USERS_BALANCE, TABLE_USERS, USERS_IGNORETOP, USERS_BALANCE));
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setBoolean(1, false);
				stm.setInt(2, limit);
				try (ResultSet rs = stm.executeQuery()) {
					while (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						list.add(new TopEntry(uuid, rs.getString(2), rs.getLong(3)));
					}
				}
			}
		}
		return list;
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
		String usersTable = this.mapQuery(String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(16) NOT NULL,"
				+ "`%s` INT UNSIGNED NOT NULL DEFAULT '0', `%s` BOOLEAN NOT NULL DEFAULT FALSE,"
				+ "PRIMARY KEY (`%s`)) ENGINE = InnoDB CHARSET=ascii COLLATE ascii_general_ci;",
				TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_IGNORETOP, USERS_UUID));
		String logsTable = this.mapQuery(String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` INT UNSIGNED NOT NULL AUTO_INCREMENT, `%s` VARCHAR(36) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,"
				+ "`%s` VARCHAR(16) NOT NULL, `%s` VARCHAR(24) NOT NULL, `%s` VARCHAR(32) NOT NULL, `%s` VARCHAR(36) NOT NULL,"
				+ "`%s` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(8) NOT NULL,"
				+ "`%s` INT UNSIGNED NOT NULL, `%s` INT UNSIGNED NOT NULL, PRIMARY KEY (`%s`), "
				+ "FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`)) ENGINE = InnoDB CHARSET=ascii COLLATE ascii_general_ci;",
				TABLE_LOGS, LOGS_ID, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER, LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_TIME, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE, LOGS_ID, LOGS_UUID, TABLE_USERS, USERS_UUID));
		try (Connection conn = this.hikari.getConnection()) {
			try (Statement stm = conn.createStatement()) {
				stm.addBatch(usersTable);
				stm.addBatch(logsTable);
				stm.executeBatch();
			}
		}
	}

}
