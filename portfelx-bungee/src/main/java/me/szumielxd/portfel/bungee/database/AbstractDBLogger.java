package me.szumielxd.portfel.bungee.database;

import java.sql.Date;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.common.objects.ActionExecutor;
import me.szumielxd.portfel.common.objects.User;

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
	 * @param targetId unique identifier of logged action target
	 * @param targetName display name of logged action target
	 * @param executor executor of logged action
	 * @param server server where logged action was triggered
	 * @param orderName description of this action
	 * @param value value of logged action
	 * @param balance target's balance before logged action
	 */
	public void handleIncomingLog(@NotNull UUID targetId, @NotNull String targetName, @NotNull ActionExecutor executor, @NotNull String server, @NotNull Date time, @NotNull String orderName, @NotNull ActionType type, long value, long balance);
	
	
	public static enum ActionType {
		ADD,
		SET,
		REMOVE;
		
		public static @Nullable ActionType parse(@NotNull String text) {
			return Stream.of(ActionType.values()).filter(t -> t.name().equalsIgnoreCase(text)).findAny().orElse(null);
		}
		
	}

}
