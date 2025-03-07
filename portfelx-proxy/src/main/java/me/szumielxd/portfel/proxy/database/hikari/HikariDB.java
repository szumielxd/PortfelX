package me.szumielxd.portfel.proxy.database.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
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
import me.szumielxd.portfel.api.managers.TopManager;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public abstract class HikariDB<C> implements AbstractDB {
	
	
	protected final PortfelProxyImpl<C> plugin;
	protected HikariDataSource hikari;
	
	private boolean tablesChecked = false;
	
	
	private final String TABLE_USERS;
	private final String TABLE_LOGS;
	
	private final String USERS_NAME;
	private final String USERS_UUID;
	private final String USERS_BALANCE;
	private final String USERS_MINORBALANCE;
	private final String USERS_IGNORETOP;
	private final String USERS_LASTJOIN;
	
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
	
	
	protected HikariDB(@NotNull PortfelProxyImpl<C> plugin) {
		this.plugin = plugin;
		Config cfg = plugin.getConfiguration();
		
		DB_HOST = cfg.getString(ProxyConfigKey.DATABASE_HOST);
		DB_NAME = cfg.getString(ProxyConfigKey.DATABASE_DATABASE);
		DB_USER = cfg.getString(ProxyConfigKey.DATABASE_USERNAME);
		DB_PASSWD = cfg.getString(ProxyConfigKey.DATABASE_PASSWORD);
		
		TABLE_USERS = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_NAME));
		TABLE_LOGS = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_NAME));
		
		USERS_NAME = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_USERNAME));
		USERS_UUID = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_UUID));
		USERS_BALANCE = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_BALANCE));
		USERS_MINORBALANCE = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_MINORBALANCE));
		USERS_IGNORETOP = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_IGNORETOP));
		USERS_LASTJOIN = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_USERS_COLLUMN_LASTJOIN));
		
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
		int port = getDefaultPort();
		if (host.length > 1) {
			try {
				port = Integer.parseInt(host[1]);
			} catch (NumberFormatException e) {
				// fallback to default port
			}
		}
		setupDatabase(config, host[0], port, DB_NAME, DB_USER, DB_PASSWD);
		
		Config cfg = plugin.getConfiguration();
		Map<String, String> properties = cfg.getStringMap(ProxyConfigKey.DATABASE_POOL_PROPERTIES);
		setupProperties(config, properties);
		
		config.setMaximumPoolSize(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MAXSIZE));
		config.setMinimumIdle(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MINIDLE));
		config.setMaxLifetime(cfg.getInt(ProxyConfigKey.DATABASE_POOL_MAXLIFETIME));
		config.setKeepaliveTime(cfg.getInt(ProxyConfigKey.DATABASE_POOL_KEEPALIVE));
		config.setConnectionTimeout(cfg.getInt(ProxyConfigKey.DATABASE_POOL_TIMEOUT));
		config.setInitializationFailTimeout(-1);
		
		hikari = new HikariDataSource(config);
		
		if (!tablesChecked) {
			try {
				setupTables();
				tablesChecked = true;
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
	public @NotNull Connection connect() throws SQLException {
		if (hikari == null) {
			throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
		}
		checkConnection();
		Connection conn = hikari.getConnection();
		if (conn == null) {
			throw new SQLException("Unable to get a connection from the pool. (connection is null)");
		}
		return conn;
	}
	
	/**
	 * Check if database is connected.
	 * 
	 * @return true if connection to database is opened
	 */
	@Override
	public boolean isConnected() {
		return isValid() && !hikari.isClosed();
	}
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection() {
		if (!isConnected()) {
			setup();
		}
	}
	
	/**
	 * Check if database connection is valid.
	 * 
	 * @return true if connection to database is valid
	 */
	@Override
	public boolean isValid() {
		return hikari != null;
	}
	
	/**
	 * Shutdown database
	 */
	@Override
	public void shutdown() {
		if (hikari != null) {
			hikari.close();
		}
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
	public @Nullable ProxyOperableUser loadUserByName(@NotNull String name, boolean strict) throws SQLException {
		String sql = mapQuery("SELECT `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` =%s ? ORDEE BY `%s` DESC LIMIT 1"
				.formatted(USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_MINORBALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_NAME, (strict ? " BINARY" : ""), USERS_LASTJOIN));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, name);
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						return new ProxyOperableUser(plugin, uuid, rs.getString(2), rs.getBoolean(5), rs.getLong(3), rs.getLong(4));
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
	public @Nullable ProxyOperableUser loadUser(@NotNull UUID uuid) throws SQLException {
		try (Connection conn = connect()) {
			return executeLoadUser(conn, uuid);
		}
	}
	
	private @Nullable ProxyOperableUser executeLoadUser(@NotNull Connection conn, @NotNull UUID uuid) throws SQLException {
		String sql = mapQuery("SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ?"
				.formatted(USERS_NAME, USERS_BALANCE, USERS_MINORBALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_UUID));
		try (PreparedStatement stm = conn.prepareStatement(sql)) {
			stm.setString(1, uuid.toString());
			try (ResultSet rs = stm.executeQuery()) {
				if (rs.next()) {
					return new ProxyOperableUser(plugin, uuid, rs.getString(1), rs.getBoolean(4), rs.getLong(2), rs.getLong(3));
				}
			}
		}
		return null;
	}
	
	private void executeUpdateUserName(@NotNull Connection conn, @NotNull UUID uuid, @NotNull String username) throws SQLException {
		String sql = mapQuery("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_NAME, USERS_UUID));
		try (PreparedStatement stm = conn.prepareStatement(sql)) {
			stm.setString(1, username);
			stm.setString(2, uuid.toString());
			stm.executeUpdate();
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
	public @NotNull ProxyOperableUser loadOrCreateUser(@NotNull UUID uuid, @NotNull String username) throws SQLException, IllegalStateException {
		try (Connection conn = connect()) {
			// load user
			ProxyOperableUser user = executeLoadUser(conn, uuid);
			if (user != null) {
				// check for name update
				if (user.getName().equals(username)) {
					executeUpdateUserName(conn, uuid, username);
					user.setName(username);
				}
				return user;
			}
			
			// create new user
			String sql = mapQuery("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?)"
					.formatted(TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_MINORBALANCE, USERS_IGNORETOP));
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				user = new ProxyOperableUser(plugin, uuid, username, false, 0, 0);
				stm.setString(1, user.getUniqueId().toString());
				stm.setString(2, user.getName());
				stm.setLong(3, user.getBalance());
				stm.setLong(4, user.getMinorBalance());
				stm.setBoolean(5, user.isDeniedInTop());
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
		if (users.length == 0) {
			return arr;
		}
		UUID[] uuids = Stream.of(users)
				.map(User::getUniqueId)
				.toArray(UUID[]::new);
		final String uuidMarks = ", ?".repeat(uuids.length).substring(2);
		final String sql = mapQuery("SELECT CAST(`pos` as INT), `%s` FROM (SELECT (@i:=@i + 1) AS `pos`, `%s` FROM `%s`, (SELECT @i:=0) AS `i` WHERE `%s` = false ORDER BY `%s` DESC) as `top` WHERE `%s` IN (%s)"
				.formatted(USERS_UUID, USERS_UUID, TABLE_USERS, USERS_IGNORETOP, USERS_BALANCE, USERS_UUID, uuidMarks));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				// fill query with UUIDs
				for (int index = 0; index < uuids.length; index++) {
					stm.setString(index + 1, uuids[index].toString());
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
		}
		return arr;
	}
	
	/**
	 * Get position of given users in minor balance top. If user doesn't exist in top, then returned position is null.
	 * 
	 * @implNote Thread unsafe.
	 * @param users array of users to get
	 * @return array of positions in the same order as given users array
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public @NotNull Integer[] getMinorTopPos(User... users) throws SQLException {
		Integer[] arr = new Integer[users.length];
		if (users.length == 0) {
			return arr;
		}
		UUID[] uuids = Stream.of(users)
				.map(User::getUniqueId)
				.toArray(UUID[]::new);
		final String uuidMarks = ", ?".repeat(uuids.length).substring(2);
		final String sql = mapQuery("SELECT CAST(`pos` as INT), `%s` FROM (SELECT (@i:=@i + 1) AS `pos`, `%s` FROM `%s`, (SELECT @i:=0) AS `i` WHERE `%s` = false ORDER BY `%s` DESC) as `top` WHERE `%s` IN (%s)"
				.formatted(USERS_UUID, USERS_UUID, TABLE_USERS, USERS_IGNORETOP, USERS_MINORBALANCE, USERS_UUID, uuidMarks));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				// fill query with UUIDs
				for (int index = 0; index < uuids.length; index++) {
					stm.setString(index + 1, uuids[index].toString());
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
		}
		return arr;
	}
	
	/**
	 * Set last join time in database to now.
	 * 
	 * @implNote Thread unsafe.
	 * @param users array of users to update
	 * @throws Exception when something went wrong
	 */
	public void bumpLastJoin(@NotNull User... users) throws SQLException {
		UUID[] uuids = Stream.of(users)
				.map(User::getUniqueId)
				.toArray(UUID[]::new);
		final String uuidMarks = ", ?".repeat(uuids.length).substring(2);
		String sql = "UPDATE `%s` SET `%s` = DEFAULT WHERE `%s` IN (%s)"
				.formatted(TABLE_USERS, USERS_LASTJOIN, USERS_UUID, uuidMarks);
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				for (int i = 1; i <= uuids.length; i++) {
					stm.setString(i, uuids[i].toString());
				}
				stm.executeUpdate();
			}
		}
	}
	
	/**
	 * Update given user.
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param users user to update
	 * @return list of all updated users
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public List<ProxyOperableUser> updateUsers(@NotNull ProxyOperableUser... users) throws SQLException {
		List<ProxyOperableUser> updatedUsers = new ArrayList<>();
		if (users.length == 0) {
			return updatedUsers;
		}
		// map users by UUID
		final Map<UUID, ProxyOperableUser> map = Stream.of(users)
				.collect(Collectors.toMap(User::getUniqueId, Function.identity(), (a, b) -> a));
		// generate right amount of `?` characters to insert into query
		final String uuidMarks = ", ?".repeat(map.size()).substring(2);
		final String sql = mapQuery("SELECT `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` IN (%s)"
				.formatted(USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_MINORBALANCE, USERS_IGNORETOP, TABLE_USERS, USERS_UUID, uuidMarks));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
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
						user.setPlainMinorBalance(rs.getLong(4));
						user.setPlainDeniedInTop(rs.getBoolean(5));
						updatedUsers.add(user);
					}
				}
			}
		}
		return updatedUsers;
	}
	
	/**
	 * Save user values marked as changed.
	 * 
	 * @implNote Internal use only, users are saved automatically.
	 * @param users users to operate on
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void saveChanges(@NotNull ProxyOperableUser... users) throws Exception {
		try (Connection conn = connect()) {
			for (var user : users) {
				synchronized (user) {
					// format items to change
					List<String> toChange = new LinkedList<>();
					if (user.isMinorBalanceChanged()) {
						toChange.add("`%s` = ?".formatted(USERS_MINORBALANCE));
					}
					
					if (!toChange.isEmpty()) {
						String sql = mapQuery("UPDATE `%s` SET %s WHERE `%s` = ?"
								.formatted(TABLE_USERS, String.join(", ", toChange), USERS_UUID));
						try (PreparedStatement stm = conn.prepareStatement(sql)) {
							int index = 0;
							// dynamically fill prepared statement 
							if (user.isMinorBalanceChanged()) {
								stm.setLong(++index, user.getMinorBalance());
							}
							stm.setString(++index, user.getUniqueId().toString());
							if (stm.executeUpdate() == 0) {
								throw new SQLException("Unable to save a userdata. (inexistent uuid)");
							}
							user.setUnchanged();
						}
					}
				}
			}
		}
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
		String sql = mapQuery("UPDATE `%s` SET `%s` = `%s` + ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_BALANCE, USERS_BALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
				}
			}
		}
	}
	
	/**
	 * Add given amount of money to minor balance of specified user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to add
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void addMinorBalance(@NotNull ProxyOperableUser user, long amount) throws Exception {
		String sql = mapQuery("UPDATE `%s` SET `%s` = `%s` + ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_MINORBALANCE, USERS_MINORBALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's minor balance. (inexistent uuid)");
				}
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
		String sql = mapQuery("UPDATE `%s` SET `%s` = `%s` - ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_BALANCE, USERS_BALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
				}
			}
		}
	}
	
	/**
	 * Take given amount of money from minor balance of specified user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param amount amount of money to take
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void takeMinorBalance(@NotNull ProxyOperableUser user, long amount) throws SQLException {
		String sql = mapQuery("UPDATE `%s` SET `%s` = `%s` - ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_MINORBALANCE, USERS_MINORBALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, amount);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
				}
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
		String sql = mapQuery("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_BALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, balance);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's balance. (inexistent uuid)");
				}
			}
		}
	}
	
	/**
	 * Set minor balance of specified user to given amount
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param user user to operate on
	 * @param balance new balance
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public void setMinorBalance(@NotNull ProxyOperableUser user, long balance) throws SQLException {
		String sql = mapQuery("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_MINORBALANCE, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setLong(1, balance);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's minor balance. (inexistent uuid)");
				}
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
		String sql = mapQuery("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?"
				.formatted(TABLE_USERS, USERS_IGNORETOP, USERS_UUID));
		try (Connection conn = connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setBoolean(1, deniedInTop);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) {
					throw new SQLException("Unable to update a user's deniedInTop state. (inexistent uuid)");
				}
			}
		}
	}
	
	/**
	 * Fetch balance top of specified size.
	 * 
	 * @implNote Internal use only, try {@link TopManager} instead. Thread unsafe.
	 * @param limit max size of top
	 * @return list of top entries sorted from first to last
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> getTop(int limit) throws SQLException {
		List<TopEntry> list = new ArrayList<>(limit);
		String sql = mapQuery("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ? ORDER BY `%s` DESC LIMIT ?"
				.formatted(USERS_UUID, USERS_NAME, USERS_BALANCE, TABLE_USERS, USERS_IGNORETOP, USERS_BALANCE));
		try (Connection conn = connect()) {
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
	 * Fetch minor balance top of specified size.
	 * 
	 * @implNote Internal use only, try {@link TopManager} instead. Thread unsafe.
	 * @param limit max size of top
	 * @return list of top entries sorted from first to last
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<TopEntry> getMinorTop(int limit) throws SQLException {
		List<TopEntry> list = new ArrayList<>(limit);
		String sql = mapQuery("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ? ORDER BY `%s` DESC LIMIT ?"
				.formatted(USERS_UUID, USERS_NAME, USERS_MINORBALANCE, TABLE_USERS, USERS_IGNORETOP, USERS_MINORBALANCE));
		try (Connection conn = connect()) {
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
	 * Check for tables existence and create them if not exists already.
	 * 
	 * @throws SQLException when cannot establish the connection to the database
	 */
	private void setupTables() throws SQLException {
		String usersTable = mapQuery(("CREATE TABLE IF NOT EXISTS `%s` (`%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(16) NOT NULL,"
				+ "`%s` INT UNSIGNED NOT NULL DEFAULT '0', `%s` INT UNSIGNED NOT NULL DEFAULT '0', `%s` BOOLEAN NOT NULL DEFAULT FALSE,"
				+ "PRIMARY KEY (`%s`)) ENGINE = InnoDB CHARSET=ascii COLLATE ascii_general_ci")
						.formatted(TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_MINORBALANCE, USERS_IGNORETOP, USERS_UUID));
		String logsTable = mapQuery(("CREATE TABLE IF NOT EXISTS `%s` (`%s` INT UNSIGNED NOT NULL AUTO_INCREMENT, `%s` VARCHAR(36) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,"
				+ "`%s` VARCHAR(16) NOT NULL, `%s` VARCHAR(24) NOT NULL, `%s` VARCHAR(32) NOT NULL, `%s` VARCHAR(36) NOT NULL,"
				+ "`%s` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(8) NOT NULL,"
				+ "`%s` INT UNSIGNED NOT NULL, `%s` INT UNSIGNED NOT NULL, PRIMARY KEY (`%s`), "
				+ "FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`)) CHARSET=ascii COLLATE ascii_general_ci")
						.formatted(TABLE_LOGS, LOGS_ID, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER, LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_TIME, LOGS_ORDERNAME,
								LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE, LOGS_ID, LOGS_UUID, TABLE_USERS, USERS_UUID));
		try (Connection conn = connect()) {
			try (Statement stm = conn.createStatement()) {
				// users
				stm.addBatch(usersTable);
				/*stm.addBatch(buildIndexQuery(TABLE_USERS, USERS_NAME));
				stm.addBatch(buildIndexQuery(TABLE_USERS, USERS_IGNORETOP));
				*/
				
				// logs
				stm.addBatch(logsTable);
				/*stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_UUID));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_USERNAME));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_SERVER));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_EXECUTOR));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_EXECUTORUUID));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_ORDERNAME));
				stm.addBatch(buildIndexQuery(TABLE_LOGS, LOGS_ACTION));
				*/
				stm.executeBatch();
			}
		}
	}
	
	private String buildIndexQuery(@NotNull String table, @NotNull String column) {
		return mapQuery(String.format("ALTER TABLE `%s` ADD INDEX `%s`(`%s`)", table, table+"|"+column, column));
	}

}
