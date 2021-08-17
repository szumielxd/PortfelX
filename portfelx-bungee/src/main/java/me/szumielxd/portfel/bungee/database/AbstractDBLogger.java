package me.szumielxd.portfel.bungee.database;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.User;
import net.kyori.adventure.text.format.TextColor;

public interface AbstractDBLogger {
	
	
	/**
	 * Initialize database logger.
	 * 
	 * @return instance
	 */
	public AbstractDBLogger init();
	
	/**
	 * Brutally kill database logger.
	 */
	public void killLogger();
	
	/**
	 * Log adding money to target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value money amount to add
	 * @throws Exception when something went wrong
	 */
	public void logBalanceAdd(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws Exception;
	
	/**
	 * Log removing money from target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value money amount to remove
	 * @throws Exception when something went wrong
	 */
	public void logBalanceTake(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws Exception;
	
	/**
	 * Log setting new state of target's balance into database.
	 * 
	 * @param target target of action
	 * @param executor action executor
	 * @param server server where action was triggered
	 * @param orderName description of this action
	 * @param value new balance
	 * @throws Exception when something went wrong
	 */
	public void logBalanceSet(@NotNull User target, @NotNull ActionExecutor executor, @NotNull String server, @NotNull String orderName, long value) throws Exception;
	
	/**
	 * Handle incoming log.
	 * 
	 * @param logEntry logged entry
	 */
	public void handleIncomingLog(@NotNull LogEntry logEntry);
	
	/**
	 * Get recent logs.
	 * 
	 * @return list of recent logs
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<LogEntry> getLogs(@Nullable String[] targets, @Nullable String[] executors, @Nullable String[] servers, @Nullable String[] orders, @Nullable ActionType[] actions, @Nullable NumericCondition[] valueConditions, @Nullable NumericCondition[] balanceConditions) throws Exception;
	
	
	public static enum ActionType {
		ADD("+", GREEN),
		SET("", GOLD),
		REMOVE("-", RED);
		
		private final TextColor color;
		private final String formattedPrefix;
		
		private ActionType(@NotNull String formattedPrefix, @NotNull TextColor color) {
			this.color = color;
			this.formattedPrefix = formattedPrefix;
		}
		
		public TextColor getColor() {
			return this.color;
		}
		
		public String getFormattedPrefix() {
			return this.formattedPrefix;
		}
		
		public static @Nullable ActionType parse(@NotNull String text) {
			return Stream.of(ActionType.values()).filter(t -> t.name().equalsIgnoreCase(text)).findAny().orElse(null);
		}
		
	}
	
	
	public static class LogEntry {
		
		
		private final int id;
		private final UUID targetId;
		private final String targetName;
		private final ActionExecutor executor;
		private final String server;
		private final Date time;
		private final String orderName;
		private final ActionType type;
		private final long value;
		private final long balance;
		
		
		public LogEntry(int id, @NotNull UUID targetId, @NotNull String targetName, @NotNull ActionExecutor executor, @NotNull String server, @NotNull Date time, @NotNull String orderName, @NotNull ActionType type, long value, long balance) {
			this.id = id;
			this.targetId = targetId;
			this.targetName = targetName;
			this.executor = executor;
			this.server = server;
			this.time = time;
			this.orderName = orderName;
			this.type = type;
			this.value = value;
			this.balance = balance;
		}
		
		
		public int getLogId() {
			return this.id;
		}
		
		/**
		 * Get action target's UUID.
		 * 
		 * @return UUID
		 */
		public UUID getTargetUniqueId() {
			return this.targetId;
		}
		
		/**
		 * Get action target's name.
		 * 
		 * @return name
		 */
		public String getTargetName() {
			return this.targetName;
		}
		
		/**
		 * Get action executor.
		 * 
		 * @return executor
		 */
		public ActionExecutor getExecutor() {
			return this.executor;
		}
		
		/**
		 * Get server where action was executed.
		 * 
		 * @return server's name
		 */
		public String getServer() {
			return this.server;
		}
		
		/**
		 * Get time when action was executed.
		 * 
		 * @return time
		 */
		public Date getTime() {
			return this.time;
		}
		
		/**
		 * Get action order's name.
		 * 
		 * @return order's name
		 */
		public String getOrderName() {
			return this.orderName;
		}
		
		/**
		 * Get action's type
		 * 
		 * @return type of action
		 */
		public ActionType getType() {
			return this.type;
		}
		
		/**
		 * Get action's value
		 * 
		 * @return value of action
		 */
		public long getValue() {
			return this.value;
		}
		
		/**
		 * Get target's balance status before action execution.
		 * 
		 * @return target's balance
		 */
		public long getBalance() {
			return this.balance;
		}
		
		
	}
	
	
	public static class NumericCondition {
		
		
		private final String format;
		private final long[] values;
		
		
		private NumericCondition(@NotNull String format, long... values) {
			if (format.chars().filter(c -> c == '?').count() != values.length) throw new IllegalArgumentException("Format must contain exact the same amount of '?' characters as length of values array.");
			this.format = format;
			this.values = values;
		}
		
		public String getFormat() {
			return this.format;
		}
		
		public long[] getValues() {
			return this.values.clone();
		}
		
		
		public static Optional<NumericCondition> parse(@NotNull String text) {
			if (text.length() == 0) return Optional.empty();
			try {
				if (text.charAt(0) == '>') return Optional.of(new NumericCondition("> ?", Long.parseLong(text.substring(1))));
				else if (text.charAt(0) == '<') return Optional.of(new NumericCondition("< ?", Long.parseLong(text.substring(1))));
				else if (text.charAt(0) == '!') return Optional.of(new NumericCondition("<> ?", Long.parseLong(text.substring(1))));
				else return Optional.of(new NumericCondition("= ?", Long.parseLong(text)));
			} catch (NumberFormatException e) {
				if (text.contains("-")) {
					String[] vals = text.split("-", 2);
					try {
						return Optional.of(new NumericCondition("BETWEEN ? AND ?", Long.parseLong(vals[0]), Long.parseLong(vals[1])));
					} catch (NumberFormatException ex) {}
				}
				return Optional.empty();
			}
		}
		
		
	}
	

}
