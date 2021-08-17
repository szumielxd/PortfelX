package me.szumielxd.portfel.api.managers;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TopManager {
	
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	public @NotNull TopManager init();
	
	/**
	 * Shutdown all operations related to userManager
	 */
	public void killManager();
	
	public boolean isInitialized();
	
	public boolean isDead();
	
	public boolean isValid();
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	public @Nullable TopEntry getByPos(int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	public @Nullable List<TopEntry> getFullTopCopy();
	
	
	public static class TopEntry {
		
		private final UUID uuid;
		private final String name;
		private final long balance;
		
		public TopEntry(@NotNull UUID uuid, @NotNull String name, long balance) {
			this.uuid = uuid;
			this.name = name;
			this.balance = balance;
		}
		
		/**
		 * Get player's UUID
		 * 
		 * @return UUID
		 */
		public @NotNull UUID getUniqueId() {
			return this.uuid;
		}
		
		/**
		 * Get player's name
		 * 
		 * @return name
		 */
		public @NotNull String getName() {
			return this.name;
		}
		
		/**
		 * Get player's balance
		 * 
		 * @return balance
		 */
		public long getBalance() {
			return this.balance;
		}
		
	}
	

}
