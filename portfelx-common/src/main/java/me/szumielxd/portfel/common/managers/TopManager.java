package me.szumielxd.portfel.common.managers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.objects.ExecutedTask;

public abstract class TopManager {
	
	
	private ExecutedTask topUpdater;
	private Boolean initialized;
	
	
	public TopManager() {
	}
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	public TopManager init() {
		this.topUpdater = this.getPlugin().getTaskManager().runTaskTimerAsynchronously(this::update, 10, 10, TimeUnit.SECONDS);
		this.initialized = true;
		return this;
	}
	
	/**
	 * Shutdown all operations related to userManager
	 */
	public void killManager() {
		if (this.topUpdater != null) this.topUpdater.cancel();
		this.topUpdater = null;
		this.initialized = false;
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
		if (this.isDead()) throw new IllegalStateException("Cannot operate on dead UserManager");
		if (!this.isInitialized()) throw new IllegalStateException("UserManager is not initialized");
	}
	
	
	/**
	 * Update top.
	 */
	protected abstract void update();
	
	/**
	 * Get Portfel instance
	 * 
	 * @return plugin
	 */
	protected abstract Portfel getPlugin();
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	public abstract TopEntry getByPos(int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	public abstract List<TopEntry> getFullTopCopy();
	
	
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
