package me.szumielxd.portfel.bungee.database.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.objects.OperableUser;
import me.szumielxd.portfel.common.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class HikariDB implements AbstractDB {
	
	
	protected final PortfelBungee plugin;
	protected HikariDataSource hikari;
	
	private boolean tablesChecked = false;
	
	
	public static String TABLE_USERS = StringEscapeUtils.escapeSql("WalutaPrawdziwa_MF");
	public static String TABLE_LOGS = StringEscapeUtils.escapeSql("PortfelLog_MF");
	
	public static String USERS_NAME = StringEscapeUtils.escapeSql("Nick");
	public static String USERS_UUID = StringEscapeUtils.escapeSql("UUID");
	public static String USERS_BALANCE = StringEscapeUtils.escapeSql("Konto");
	public static String USERS_INTOP = StringEscapeUtils.escapeSql("IGNORE_TOP");
	
	public static String LOGS_ID = StringEscapeUtils.escapeSql("id");
	public static String LOGS_UUID = StringEscapeUtils.escapeSql("uuid");
	public static String LOGS_USERNAME = StringEscapeUtils.escapeSql("username");
	public static String LOGS_SERVER = StringEscapeUtils.escapeSql("server");
	public static String LOGS_EXECUTOR = StringEscapeUtils.escapeSql("executor");
	public static String LOGS_EXECUTORUUID = StringEscapeUtils.escapeSql("executor_uuid");
	public static String LOGS_TIME = StringEscapeUtils.escapeSql("time");
	public static String LOGS_ORDERNAME = StringEscapeUtils.escapeSql("order_name");
	public static String LOGS_ACTION = StringEscapeUtils.escapeSql("action");
	public static String LOGS_VALUE = StringEscapeUtils.escapeSql("value");
	public static String LOGS_BALANCE = StringEscapeUtils.escapeSql("balance");
	
	private static String DB_HOST = "localhost";
	private static String DB_NAME = "wallet";
	private static String DB_USER = "root";
	private static String DB_PASSWD = "";

	
	public HikariDB(PortfelBungee plugin) {
		this.plugin = plugin;
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
	public void setup(@NotNull PortfelBungee plugin) {
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
		
		Map<String, String> properties = new HashMap<>();
		this.setupProperties(config, properties);
		
		config.setMaximumPoolSize(10);
		config.setMinimumIdle(10);
		config.setMaxLifetime(1800000);
		config.setKeepaliveTime(0);
		config.setConnectionTimeout(5000);
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
		String sql = String.format("SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` =%s ?", USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_NAME, (strict ? " BINARY" : ""));
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, name);
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
						new OperableUser(this.plugin, uuid, rs.getString(2), player != null && player.isConnected(), rs.getBoolean(4), rs.getLong(3));
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
		String sql = String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ?", USERS_NAME, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_UUID);
		ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, uuid.toString());
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						return new OperableUser(this.plugin, uuid, rs.getString(1), player != null && player.isConnected(), rs.getBoolean(3), rs.getLong(2));
					}
				}
			}
			
			// fallback to old username offline-mode system
			if (!MiscUtils.isOnlineModeUUID(uuid) && player != null) {
				sql = String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = BINARY ? AND `%s` IS NULL", USERS_UUID, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_NAME, USERS_UUID);
				try (PreparedStatement stm = conn.prepareStatement(sql)) {
					stm.setString(1, player.getName());
					try (ResultSet rs = stm.executeQuery()) {
						if (rs.next()) {
							return new OperableUser(this.plugin, uuid, rs.getString(1), player.isConnected(), rs.getBoolean(3), rs.getLong(2));
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
		String sql = String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = ?", USERS_NAME, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_UUID);
		ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
		OperableUser user = null;
		try (Connection conn = this.connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, uuid.toString());
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						user = new OperableUser(this.plugin, uuid, rs.getString(1), player != null && player.isConnected(), rs.getBoolean(3), rs.getLong(2));
					}
				}
			}
			
			if (user != null) {
				if (user.getName() != player.getName()) {
					user.setName(player.getName());
					sql = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?", TABLE_USERS, USERS_NAME, USERS_UUID);
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
				sql = String.format("SELECT `%s`, `%s`, `%s` FROM `%s` WHERE `%s` = BINARY ? AND `%s` IS NULL", USERS_UUID, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_NAME, USERS_UUID);
				try (PreparedStatement stm = conn.prepareStatement(sql)) {
					stm.setString(1, player.getName());
					try (ResultSet rs = stm.executeQuery()) {
						if (rs.next()) {
							user = new OperableUser(this.plugin, uuid, rs.getString(1), player.isConnected(), rs.getBoolean(3), rs.getLong(2));
						}
					}
				}
				if (user != null) {
					sql = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = BINARY ? AND `%s` IS NULL LIMIT 1", TABLE_USERS, USERS_UUID, USERS_NAME, USERS_UUID, USERS_NAME, USERS_UUID);
					try (PreparedStatement stm = conn.prepareStatement(sql)) {
						stm.setString(1, player.getName());
						stm.executeQuery();
					}
					return user;
				}
			}
			
			// create new user
			sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)", TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_INTOP);
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				if (user == null) user = new OperableUser(this.plugin, uuid, player.getName(), player.isConnected(), false, 0);
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
	 * Update given user
	 * 
	 * @implNote Internal use only, try {@link User} instead. Thread unsafe.
	 * @param users user to update
	 * @return list of all updated users
	 * @throws SQLException when cannot establish the connection to the database
	 */
	@Override
	public List<OperableUser> updateUsers(@NotNull OperableUser... users) throws SQLException {
		Validate.noNullElements(users);
		List<OperableUser> updatedUsers = Collections.emptyList();
		if (users.length == 0) return updatedUsers;
		this.checkConnection();
		
		// map users by UUID
		final Map<UUID, OperableUser> map = Stream.of(users).collect(Collectors.toMap(User::getUniqueId, Function.identity(), (a, b) -> a));
		// generate right amount of `?` characters to insert into query
		final String uuidMarks = String.join(", ", map.keySet().stream().map(s -> "?").toArray(String[]::new));
		final String sql = String.format("SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` IN (%s)", USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_INTOP, TABLE_USERS, USERS_UUID, uuidMarks);
		try (Connection conn = this.connect(); PreparedStatement stm = conn.prepareStatement(sql)) {
			int index = 0;
			// fill query with UUIDs
			for (UUID uuid : map.keySet()) {
				stm.setString(++index, uuid.toString());
			}
			try (ResultSet rs = stm.executeQuery()) {
				while (rs.next()) {
					UUID uuid = UUID.fromString(rs.getString(1));
					OperableUser user = map.get(uuid);
					user.setName(rs.getString(2));
					user.setPlainBalance(rs.getLong(3));
					user.setDeniedInTop(rs.getBoolean(4));
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
	public void addBalance(@NotNull OperableUser user, long amount) throws Exception {
		this.checkConnection();
		String sql = String.format("UPDATE `%s` SET `%s` = `%s` + ? WHERE `%s` = ?", TABLE_USERS, USERS_BALANCE, USERS_UUID);
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
	public void takeBalance(@NotNull OperableUser user, long amount) throws SQLException {
		this.checkConnection();
		String sql = String.format("UPDATE `%s` SET `%s` = `%s` - ? WHERE `%s` = ?", TABLE_USERS, USERS_BALANCE, USERS_UUID);
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
	public void setBalance(@NotNull OperableUser user, long balance) throws SQLException {
		this.checkConnection();
		String sql = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?", TABLE_USERS, USERS_BALANCE, USERS_UUID);
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
	public void setDeniedInTop(@NotNull OperableUser user, boolean deniedInTop) throws SQLException {
		this.checkConnection();
		String sql = String.format("UPDATE `%s` SET `%s` = `%s` - ? WHERE `%s` = ?", TABLE_USERS, USERS_INTOP, USERS_UUID);
		try (Connection conn = this.hikari.getConnection()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setBoolean(1, deniedInTop);
				stm.setString(2, user.getUniqueId().toString());
				if (stm.executeUpdate() == 0) throw new SQLException("Unable to update a user's deniedInTop state. (inexistent uuid)");
			}
		}
	}
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection() {
		if (this.isConnected()) this.setup(this.plugin);
	}
	
	/**
	 * Check for tables existence and create them if not exists already.
	 * 
	 * @throws SQLException when cannot establish the connection to the database
	 */
	private void setupTables() throws SQLException {
		this.checkConnection();
		String usersTable = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(16) NOT NULL,"
				+ "`%s` UNSIGNED INT NOT NULL DEFAULT '0' , `%s` BOOLEAN NOT NULL DEFAULT FALSE,"
				+ "PRIMARY KEY (`%s`)) ENGINE = InnoDB;",
				TABLE_USERS, USERS_UUID, USERS_NAME, USERS_BALANCE, USERS_INTOP, USERS_UUID);
		String logsTable = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` INT UNSIGNED NOT NULL AUTO_INCREMENT, `%s` VARCHAR(36) NOT NULL,"
				+ "`%s` VARCHAR(16) NOT NULL, `%s` VARCHAR(24) NOT NULL, `%s` VARCHAR(32) NOT NULL, `%s` VARCHAR(36) NOT NULL,"
				+ "`%s` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, `%s` VARCHAR(36) NOT NULL, `%s` VARCHAR(8) NOT NULL,"
				+ "`%s` INT UNSIGNED NOT NULL, `%s` INT UNSIGNED NOT NULL, PRIMARY KEY (`%s`)),"
				+ "FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ENGINE = InnoDB CHARSET=ascii COLLATE ascii_general_ci;",
				TABLE_LOGS, LOGS_ID, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER, LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_TIME, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE, LOGS_ID, LOGS_UUID, TABLE_USERS, USERS_UUID);
		
		try (Connection conn = this.hikari.getConnection()) {
			try (Statement stm = conn.createStatement()) {
				stm.addBatch(logsTable);
				stm.addBatch(usersTable);
				stm.executeBatch();
			}
		}
	}

}
