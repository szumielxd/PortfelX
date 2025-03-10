package me.szumielxd.portfel.common.managers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.managers.TopManager;
import me.szumielxd.portfel.api.objects.ExecutedTask;

public abstract class TopManagerImpl<C> implements TopManager {
	
	
	private ExecutedTask topUpdater;
	private Boolean initialized;
	
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public @NotNull TopManager init() {
		this.topUpdater = this.getPlugin().getTaskManager().runTaskTimerAsynchronously(this::update, 10, 10, TimeUnit.SECONDS);
		this.initialized = true;
		return this;
	}
	
	/**
	 * Shutdown all operations related to userManager
	 */
	@Override
	public void killManager() {
		if (this.topUpdater != null) {
			this.topUpdater.cancel();
		}
		this.topUpdater = null;
		this.initialized = false;
	}
	
	@Override
	public boolean isInitialized() {
		return this.initialized != null;
	}
	
	@Override
	public boolean isDead() {
		return isInitialized() && !this.initialized;
	}
	
	@Override
	public boolean isValid() {
		return isInitialized() && this.initialized;
	}
	
	protected void validate() {
		if (this.isDead()) throw new IllegalStateException("Cannot operate on dead UserManager");
		if (!this.isInitialized()) throw new IllegalStateException("UserManager is not initialized");
	}
	
	
	/**
	 * Update top.
	 */
	protected abstract CompletableFuture<Void> update();
	
	/**
	 * Get Portfel instance
	 * 
	 * @return plugin
	 */
	protected abstract @NotNull Portfel<C> getPlugin();
	

}
