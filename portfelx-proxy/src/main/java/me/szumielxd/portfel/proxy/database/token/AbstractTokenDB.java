package me.szumielxd.portfel.proxy.database.token;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.proxy.objects.PrizeToken;

public interface AbstractTokenDB {
	
	
	/**
	 * Get name of database's type
	 * 
	 * @return database type's name
	 */
	public @NotNull String getDBName();
	
	/**
	 * Setup database connection properties.
	 */
	public void setup();
	
	/**
	 * Get database connection.
	 * 
	 * @return database connection
	 * @throws Exception when something went wrong
	 */
	public Connection connect() throws Exception;
	
	/**
	 * Check if database is connected.
	 * 
	 * @return true if connection to database is opened
	 */
	public boolean isConnected();
	
	/**
	 * Check if database connection is valid.
	 * 
	 * @return true if connection to database is valid
	 */
	public boolean isValid();
	
	/**
	 * Shutdown database.
	 */
	public void shutdown();
	
	/**
	 * Load prize related to given token.
	 * 
	 * @implNote Thread unsafe.
	 * @param  token string unique token
	 * @return prize assigned to this token
	 * @throws Exception when something went wrong
	 */
	public @Nullable PrizeToken getToken(@NotNull String token) throws Exception;
	
	/**
	 * Remove token from database. Used to set token as done.
	 * 
	 * @implNote Thread unsafe.
	 * @param token string token
	 * @return true if token was removed from database, otherwise false
	 * @throws Exception when something went wrong
	 */
	public boolean destroyToken(@NotNull String token) throws Exception;
	
	/**
	 * Register new prize with unique token.
	 * 
	 * @param token unique string token
	 * @param servers list of servers when this token can be executed
	 * @param order order to execute on success token match
	 * @param creator creator of this prize
	 * @param expiration date timestamp when token will expire, set to -1 for non-expiring token
	 * @throws Exception when something went wrong
	 */
	public void registerToken(@NotNull String token, @NotNull String servers, @NotNull String order, @NotNull ActionExecutor creator, long expiration) throws Exception;
	
	/**
	 * Get all active gift-codes.
	 * 
	 * @param servers servers where token can be used
	 * @param orders list of filtered orders
	 * @param creators list of creators to filter
	 * @param creationDateConditions creation date filter
	 * @param expirationDateConditions expiration date filter. Type -1 for non-expiring
	 * @return list of valid prize tokens
	 * @throws Exception when something went wrong
	 */
	public @NotNull List<PrizeToken> getTokens(@Nullable String[] servers, @Nullable String[] orders, @Nullable String[] creators, @Nullable DateCondition[] creationDateConditions, @Nullable DateCondition[] expirationDateConditions) throws Exception;
	
	/**
	 * Remove expired tokens from database.
	 * 
	 * @implNote Thread unsafe.
	 * @throws Exception when something went wrong
	 */
	public void cleanupExpired() throws Exception;
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection();
	
	
	public static class DateCondition {
		
		
		public static Pattern FORMAT_PATTERN = Pattern.compile("-1|(0|[1-9]\\d*)(s|m|h|d|mo|y)?", Pattern.CASE_INSENSITIVE);
		
		
		private final String format;
		private final long[] values;
		
		
		private DateCondition(@NotNull String format, long... values) {
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
		
		
		public static Optional<DateCondition> parse(@NotNull String text) {
			if (text.length() == 0) return Optional.empty();
			try {
				if (text.charAt(0) == '>') return Optional.of(new DateCondition("> ?", parseTime(text.substring(1))));
				else if (text.charAt(0) == '<') return Optional.of(new DateCondition("< ?", parseTime(text.substring(1))));
				else if (text.charAt(0) == '!') return Optional.of(new DateCondition("<> ?", parseTime(text.substring(1))));
				else return Optional.of(new DateCondition("= ?", parseTime(text)));
			} catch (NumberFormatException e) {
				if (text.contains("-")) {
					String[] vals = text.split("-", 2);
					try {
						return Optional.of(new DateCondition("BETWEEN ? AND ?", parseTime(vals[0]), parseTime(vals[1])));
					} catch (NumberFormatException ex) {}
				}
				return Optional.empty();
			}
		}
		
		
		public static long parseTime(String date) {
			date = date.toLowerCase();
			Matcher match = FORMAT_PATTERN.matcher(date);
			if (!match.matches()) throw new IllegalArgumentException(String.format("date must match regex `%s`, but `%s` doesn't", FORMAT_PATTERN.pattern(), date));
			long unix = date.equals("-1")? unix = -1L : Long.parseLong(match.group(1));
			if (match.group(2) != null) {
				String unit = match.group(2);
				long time = 1; // default to seconds
				if (unit.equals("m")) time = 60;
				else if (unit.equals("h")) time = 60*60;
				else if (unit.equals("d")) time = 60*60*24;
				else if (unit.equals("mo")) time = 60*60*24*30;
				else if (unit.equals("y")) time = 60*60*24*365;
				unix = System.currentTimeMillis() + unix*1000*time;
			}
			return unix;
		}
		
		
	}
	

}
