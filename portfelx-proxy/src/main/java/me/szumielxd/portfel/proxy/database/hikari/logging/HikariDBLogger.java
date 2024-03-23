package me.szumielxd.portfel.proxy.database.hikari.logging;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;

import me.szumielxd.portfel.api.configuration.Config;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger;
import me.szumielxd.portfel.proxy.database.hikari.HikariDB;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class HikariDBLogger<C> implements AbstractDBLogger {
	
	
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
	
	
	private final PortfelProxyImpl<C> plugin;
	private long lastID = -1;
	private ExecutedTask logListener;
	private Boolean initialized;
	
	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	public HikariDBLogger(PortfelProxyImpl<C> plugin) {
		this.plugin = plugin;
		Config cfg = this.plugin.getConfiguration();
		
		TABLE_LOGS = escapeSql(cfg.getString(ProxyConfigKey.DATABASE_TABLE_LOGS_NAME));
		
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
	
	
	public boolean isInitialized() {
		return this.initialized != null;
	}
	
	public boolean isDead() {
		return Boolean.FALSE.equals(this.initialized);
	}
	
	public boolean isValid() {
		return Boolean.TRUE.equals(this.initialized);
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
			AbstractDB db = this.plugin.getDatabase();
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
								Calendar currentCalendar = Calendar.getInstance();
								this.handleIncomingLog(new LogEntry(rs.getInt(1), UUID.fromString(rs.getString(2)), rs.getString(3), executor,
										rs.getString(4), new Date(rs.getTimestamp(7, currentCalendar).getTime()), rs.getString(8), type, rs.getLong(10), rs.getLong(11)));
							}
						}
						if (this.lastID < 0) this.lastID = 0;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
		AbstractDB db = this.plugin.getDatabase();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getUniqueId().toString());
				stm.setString(2, target.getName());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.ADD.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
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
		AbstractDB db = this.plugin.getDatabase();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getUniqueId().toString());
				stm.setString(2, target.getName());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.REMOVE.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
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
		AbstractDB db = this.plugin.getDatabase();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getUniqueId().toString());
				stm.setString(2, target.getName());
				stm.setString(3, server);
				stm.setString(4, executor.getDisplayName());
				stm.setString(5, executor.getUniqueId().toString());
				stm.setString(6, orderName);
				stm.setString(7, ActionType.SET.name());
				stm.setLong(8, value);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
		}
	}
	
	/**
	 * Log use of existent token.
	 * 
	 * @param target target of action
	 * @param server server where token was used
	 * @param prize executed prize
	 * @throws SQLException when cannot establish connection to database
	 */
	@Override
	public void logTokenUse(@NotNull User target, @NotNull String server, @NotNull PrizeToken prize) throws SQLException {
		this.validate();
		AbstractDB db = this.plugin.getDatabase();
		db.checkConnection();
		String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				TABLE_LOGS, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE);
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				stm.setString(1, target.getUniqueId().toString());
				stm.setString(2, target.getName());
				stm.setString(3, server);
				stm.setString(4, prize.getCreator().getDisplayName());
				stm.setString(5, prize.getCreator().getUniqueId().toString());
				stm.setString(6, String.format("%s (%s)", prize.getToken(), prize.getOrder()));
				stm.setString(7, ActionType.TOKEN.name());
				stm.setLong(8, 0);
				stm.setLong(9, target.getBalance());
				stm.executeUpdate();
			}
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
	public void handleIncomingLog(@NotNull LogEntry log) {
		this.validate();
		this.plugin.getCommonServer().getPlayers().forEach(player -> {
			if (player.isConnected() && player.hasPermission("portfel.verbose")) {
				User user = this.plugin.getUserManager().getUser(player.getUniqueId());
				if (user != null) {
					Component prefix = MiscUtils.PREFIX.append(LangKey.LOG_PREFIX.component(DARK_AQUA)).append(Component.text(" > ", GRAY));
					Component exec = this.prepareInteractive(Component.text(log.getExecutor().getDisplayName() + (Objects.equal(user.getServerName(), log.getServer()) ? "" : ("@" + log.getServer())), GREEN), log.getExecutor().getDisplayName(), log.getExecutor().getUniqueId());
					Component target = this.prepareInteractive(Component.text(log.getTargetName(), AQUA), log.getTargetName(), log.getTargetUniqueId());
					Component valComp = Component.text(log.getType().format(String.valueOf(log.getValue())), log.getType().getColor()).hoverEvent(Component.text(log.getType().format(String.valueOf(log.getValue())), log.getType().getColor())
							.append(Component.newline()).append(LangKey.LOG_VALUE_ACTION.component(GRAY, Component.text(log.getType().name(), AQUA))).append(Component.newline())
							.append(LangKey.LOG_VALUE_OLD_BALANCE.component(GRAY, Component.text(log.getBalance(), AQUA))));
					Component line1 = prefix.append(Component.text("(", DARK_GRAY)).append(exec).append(Component.text(") [", DARK_GRAY)).append(target).append(Component.text("]", DARK_GRAY));
					Component line2 = prefix.append(Component.text(log.getOrderName(), WHITE).hoverEvent(Component.text(log.getOrderName(), WHITE).append(Component.newline())
							.append(LangKey.LOG_VALUE_DATE.component(GRAY, Component.text(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(log.getTime().getTime())), AQUA))))).append(Component.space()).append(valComp);
					
					player.sendTranslated(line1);
					player.sendTranslated(line2);
				}
			}
		});
	}
	
	/**
	 * Get recent logs.
	 * 
	 * @return list of recent logs
	 * @throws SQLException when something went wrong
	 */
	@Override
	public @NotNull List<LogEntry> getLogs(@Nullable String[] targets, @Nullable String[] executors, @Nullable String[] servers, @Nullable String[] orders, @Nullable ActionType[] actions, @Nullable NumericCondition[] valueConditions, @Nullable NumericCondition[] balanceConditions) throws SQLException {
		this.validate();
		
		// parse target and executor
		List<String> targetNames = new ArrayList<>();
		List<UUID> targetIds = new ArrayList<>();
		List<String> executorNames = new ArrayList<>();
		List<UUID> executorIds = new ArrayList<>();
		if (targets != null) Stream.of(targets).forEach(str -> {
			try {targetIds.add(UUID.fromString(str));} catch (IllegalArgumentException e) {targetNames.add(str);}
		});
		if (executors != null) Stream.of(executors).forEach(str -> {
			try {executorIds.add(UUID.fromString(str));} catch (IllegalArgumentException e) {executorNames.add(str);}
		});
		
		// create builder
		StringBuilder whereClause = new StringBuilder();
		
		// targets
		if (!targetNames.isEmpty()) whereClause.append(whereClause.length()>0? " AND `" : " `").append(LOGS_USERNAME).append('`').append(" IN (").append(String.join(", ", targetNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		if (!targetIds.isEmpty()) whereClause.append(whereClause.length()>0? " AND `" : " `").append(LOGS_UUID).append('`').append(" IN (").append(String.join(", ", targetNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		// executors
		if (!executorNames.isEmpty()) whereClause.append(whereClause.length()>0? " AND `" : " `").append(LOGS_EXECUTOR).append('`').append(" IN (").append(String.join(", ", targetNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		if (!executorIds.isEmpty()) whereClause.append(whereClause.length()>0? " AND `" : " `").append(LOGS_EXECUTORUUID).append('`').append(" IN (").append(String.join(", ", targetNames.stream().map(s -> "?").toArray(String[]::new))).append(')');
		// servers
		if (servers != null && servers.length > 0) whereClause.append(whereClause.length()>0? " AND (" : " (").append(String.join(" OR ", Stream.of(servers).map(s -> String.format("`%s` LIKE ?", LOGS_SERVER)).toArray(String[]::new))).append(')');
		// orders
		if (orders != null && orders.length > 0) whereClause.append(whereClause.length()>0? " AND (" : " (").append(String.join(" OR ", Stream.of(orders).map(s -> String.format("`%s` LIKE ?", LOGS_ORDERNAME)).toArray(String[]::new))).append(')');
		// actions
		if (actions != null && actions.length > 0) whereClause.append(whereClause.length()>0? " AND (" : " (").append(String.join(" OR ", Stream.of(actions).map(s -> String.format("`%s` LIKE ?", LOGS_ACTION)).toArray(String[]::new))).append(')');
		// value
		if (valueConditions != null && valueConditions.length > 0) {
			whereClause.append(whereClause.length()>0? " AND (" : " (").append(String.join(" AND ", Stream.of(valueConditions).map(c -> String.format("`%s` %s", LOGS_VALUE, c.getFormat())).toArray(String[]::new))).append(')');
		}
		// balance
		if (balanceConditions != null && balanceConditions.length > 0) {
			whereClause.append(whereClause.length()>0? " AND (" : " (").append(String.join(" AND ", Stream.of(balanceConditions).map(c -> String.format("`%s` %s", LOGS_BALANCE, c.getFormat())).toArray(String[]::new))).append(')');
		}
		
		AbstractDB db = this.plugin.getDatabase();
		db.checkConnection();
		String sql = String.format("SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s`",
				LOGS_ID, LOGS_UUID, LOGS_USERNAME, LOGS_SERVER,
				LOGS_EXECUTOR, LOGS_EXECUTORUUID, LOGS_TIME, LOGS_ORDERNAME,
				LOGS_ACTION, LOGS_VALUE, LOGS_BALANCE, TABLE_LOGS);
		if (whereClause.length() > 0) sql = whereClause.insert(0, " WHERE ").insert(0, sql).toString();
		
		try (Connection conn = ((HikariDB)db).connect()) {
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				int i = 0;
				// targets
				if (!targetNames.isEmpty()) for (String s : targetNames) stm.setString(++i, s);
				if (!targetIds.isEmpty()) for (UUID s : targetIds) stm.setString(++i, s.toString());
				// executors
				if (!executorNames.isEmpty()) for (String s : executorNames) stm.setString(++i, s);
				if (!executorIds.isEmpty()) for (UUID s : executorIds) stm.setString(++i, s.toString());
				// servers
				if (servers != null && servers.length > 0) for (String s : servers) stm.setString(++i, this.likeContains(s));
				// orders
				if (orders != null && orders.length > 0) for (String s : orders) stm.setString(++i, this.likeContains(s));
				// actions
				if (actions != null && actions.length > 0) for (ActionType s : actions) stm.setString(++i, this.likeContains(s.name()));
				// value
				if (valueConditions != null && valueConditions.length > 0) for (NumericCondition c : valueConditions) for (long val : c.getValues()) stm.setLong(++i, val);
				// balance
				if (balanceConditions != null && balanceConditions.length > 0) for (NumericCondition c : balanceConditions) for (long val : c.getValues()) stm.setLong(++i, val);
				
				try (ResultSet rs = stm.executeQuery()) {
					List<LogEntry> list = new ArrayList<>(rs.getFetchSize());
					Calendar currentCalendar = Calendar.getInstance();
					while (rs.next()) {
						ActionExecutor executor = new ActionExecutor(rs.getString(5), UUID.fromString(rs.getString(6))) {};
						ActionType type = ActionType.parse(rs.getString(9));
						list.add(new LogEntry(rs.getInt(1), UUID.fromString(rs.getString(2)), rs.getString(3), executor,
								rs.getString(4), new Date(rs.getTimestamp(7, currentCalendar).getTime()), rs.getString(8), type, rs.getLong(10), rs.getLong(11)));
					}
					return list;
				}
			}
		}
	}
	
	
	
	private @NotNull Component prepareInteractive(@NotNull Component comp, @NotNull String name, @NotNull UUID uuid) {
		return comp.hoverEvent(Component.text(uuid.toString(), AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.LOG_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.LOG_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand(name)).insertion(uuid.toString());
	}
	
	private @NotNull String escapeLikeWildcards(@NotNull String text) {
		return text.replace("%", "\\%").replace("_", "\\_");
	}
	
	private @NotNull String likeContains(@NotNull String text) {
		return "%" + this.escapeLikeWildcards(text) + "%";
	}
	

}
