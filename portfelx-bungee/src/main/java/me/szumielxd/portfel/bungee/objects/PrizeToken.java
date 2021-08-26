package me.szumielxd.portfel.bungee.objects;

import java.util.Date;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.ActionExecutor;

public class PrizeToken {
	
	
	private final String token;
	private final Set<String> serverNames;
	private final ServerSelectorType selectType;
	private final String order;
	private final ActionExecutor creator;
	private final Date creationDate;
	private final long expiration;
	
	
	public PrizeToken(@NotNull String token, @NotNull Set<String> serverNames, @NotNull ServerSelectorType selectType, @NotNull String order, @NotNull ActionExecutor creator, @NotNull Date creationDate, long expiration) {
		this.token = token;
		this.serverNames = Collections.unmodifiableSet(serverNames);
		this.selectType = selectType;
		this.order = order;
		this.creator = creator;
		this.creationDate = creationDate;
		this.expiration = expiration;
	}
	
	
	/**
	 * Get token ID
	 * 
	 * @return token string
	 */
	public @NotNull String getToken() {
		return this.token;
	}
	
	/**
	 * Get list of all available servers where this prize is allowed.
	 * 
	 * @return list of servers
	 */
	public @NotNull Set<String> getServerNames() {
		return this.serverNames;
	}
	
	/**
	 * Get type of server selector.
	 * 
	 * @return selector type
	 */
	public @NotNull ServerSelectorType getSelectorType() {
		return this.selectType;
	}
	
	/**
	 * Get order assigned to this prize
	 * 
	 * @return order string
	 */
	public @NotNull String getOrder() {
		return this.order;
	}
	
	/**
	 * Get creator of this prize
	 * 
	 * @return creator
	 */
	public @NotNull ActionExecutor getCreator() {
		return this.creator;
	}
	
	/**
	 * Get date when this token was created
	 * 
	 * @return creation date
	 */
	public @NotNull Date getCreationDate() {
		return this.creationDate;
	}
	
	/**
	 * Get expiration date
	 * 
	 * @return -1 if not expiring, otherwise Unix timestamp
	 */
	public long getExpiration() {
		return this.expiration;
	}
	
	
	public enum ServerSelectorType {
		ANY,
		REGISTERED,
		WHITELIST
	}
	

}
