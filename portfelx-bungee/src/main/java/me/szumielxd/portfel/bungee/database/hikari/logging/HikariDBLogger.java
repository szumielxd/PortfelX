package me.szumielxd.portfel.bungee.database.hikari.logging;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.BungeeConfigKey;
import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.database.AbstractDB;
import me.szumielxd.portfel.bungee.database.AbstractDBLogger;
import me.szumielxd.portfel.bungee.database.hikari.HikariDB;
import me.szumielxd.portfel.common.Config;
import me.szumielxd.portfel.common.objects.ActionExecutor;
import me.szumielxd.portfel.common.objects.ExecutedTask;
import me.szumielxd.portfel.common.objects.User;

public class HikariDBLogger implements AbstractDBLogger {
	
	
	private final String TABLE_LOGS;
	
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
	
	
	private final PortfelBungee plugin;
	private long lastID = -1;
	private ExecutedTask logListener;
	private Boolean initialized;
	
	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	public HikariDBLogger(PortfelBungee plugin) {
		this.plugin = plugin;
		Config cfg = this.plugin.getConfiguration();
		
		TABLE_LOGS = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_NAME));
		
		LOGS_ID = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ID));
		LOGS_UUID = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_UUID));
		LOGS_USERNAME = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_USERNAME));
		LOGS_SERVER = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_SERVER));
		LOGS_EXECUTOR = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_EXECUTOR));
		LOGS_EXECUTORUUID = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_EXECUTORUUID));
		LOGS_TIME = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_TIME));
		LOGS_ORDERNAME = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ORDERNAME));
		LOGS_ACTION = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_ACTION));
		LOGS_VALUE = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_VALUE));
		LOGS_BALANCE = escapeSql(cfg.getString(BungeeConfigKey.DATABASE_TABLE_LOGS_COLLUMN_BALANCE));
		
	}
	
	
	public boolean isInitialized() {
		return this.initialized != null;
	}
	
	public boolean isDead() {
		return this.initialized == false;
	}
	
	public boolean isValid() {
		return this.initialized == true;
	}
	
	protected void validate() {
		if (this.isDead()) throw new IllegalStateException("Cannot operate on dead DBLogger");
		if (!this.isInitialized()) throw new IllegalStateException("DBLogger is not initialized");
	}
	
	/**
	 * Initialize database logger.
	 * 
	 * @return instance
	 */
	public AbstractDBLogger init() {
		this.logListener = this.plugin.getTaskManager().runTaskTimerAsynchronously(() -> {
			AbstractDB db = this.plugin.getDB();
			db.checkConnection();
			long id = this.lastID; // multithread safety
			final String sql = id < 0 ? String.format("SELECT `%s` FROM `%s` ORDER BY `%s` DESC LIMIT 1", LOGS_ID, TABLE_LOGS, LOGS_ID)
					: String.format("SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` WHERE `%s` > ? ORDER BY `%s` ASC",
							LOGS_ID, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER, LOGS_EXECUTOR,
							LOGS_EXECUTORUUID, LOGS_TIME, LOGS_ORDERNAME, LOGS_ACTION,
							LOGS_VALUE, LOGS_BALANCE, TABLE_LOGS, LOGS_ID, LOGS_ID);
			try (Connection conn = ((HikariDB)db).connect()) {
				try (PreparedStatement stm = conn.prepareStatement(sql)) {
					if (id >= 0) stm.setLong(1, id);
					try (ResultSet rs = stm.executeQuery()) {
						while (rs.next()) {
							this.lastID = rs.getLong(1);
							if (id >= 0) {
								ActionExecutor executor = new ActionExecutor(rs.getString(5), UUID.fromString(rs.getString(6))) {};
								ActionType type = ActionType.parse(rs.getString(9));
								this.handleIncomingLog(UUID.fromString(rs.getString(2)), rs.getString(3), executor,
										rs.getString(4), rs.getDate(7), rs.getString(8), type, rs.getLong(10), rs.getLong(11));
							}
						}
						if (this.lastID < 0) this.lastID = 0;
					}
				}
			} catch (SQLException e) {}
		}, 1, 1, TimeUnit.SECONDS);
		this.initialized = true;
		return this;
	}
	
	/**
	 * Brutally kill database logger.
	 */
	public void killLogger() {
		if (this.logListener != null) this.logListener.cancel();
		this.logListener = null;
		this.initialized = false;
	}
	
	/**
	 * Log adding money to target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value money amount to add
	 * @throws SQLException when cannot establish connection to database
	 */
	@Override
	public void logBalanceAdd(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws SQLException {
		this.validate();
		AbstractDB db = this.plugin.getDB();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` () VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getName());
				stm.setString(2, target.getUniqueId().toString());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.ADD.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Log removing money from target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value money amount to remove
	 * @throws SQLException when cannot establish connection to database
	 */
	@Override
	public void logBalanceTake(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws SQLException {
		this.validate();
		AbstractDB db = this.plugin.getDB();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` () VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getName());
				stm.setString(2, target.getUniqueId().toString());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.REMOVE.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Log setting new state of target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value new balance
	 * @throws SQLException when cannot establish connection to database
	 */
	@Override
	public void logBalanceSet(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws SQLException {
		this.validate();
		AbstractDB db = this.plugin.getDB();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` () VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getName());
				stm.setString(2, target.getUniqueId().toString());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.SET.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Handle incoming log.
	 * 
	 * @param targetId unique identifier of logged action target
	 * @param targetName display name of logged action target
	 * @param executor executor of logged action
	 * @param server server where logged action was triggered
	 * @param orderName description of this action
	 * @param value value of logged action
	 * @param balance target's balance before logged action
	 */
	@Override
	public void handleIncomingLog(@NotNull UUID targetId, @NotNull String targetName, @NotNull ActionExecutor executor, @NotNull String server, @NotNull Date time, @NotNull String orderName, @NotNull ActionType type, long value, long balance) {
		this.validate();
	}
	

}
