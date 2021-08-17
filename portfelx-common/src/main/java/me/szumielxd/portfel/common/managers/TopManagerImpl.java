package me.szumielxd.portfel.common.managers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.managers.TopManager;
import me.szumielxd.portfel.api.objects.ExecutedTask;

public abstract class TopManagerImpl implements TopManager {
	
	
	private ExecutedTask topUpdater;
	private Boolean initialized;
	
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public @NotNull TopManagerImpl init() {
		this.topUpdater = this.getPlugin().getTaskManager().runTaskTimerAsynchronously(this::update, 10, 10, TimeUnit.SECONDS);
		this.initialized = true;
		return this;
	}
	
	/**
	 * Shutdown all operations related to userManager
	 */
	@Override
	public void killManager() {
		if (this.topUpdater != null) this.topUpdater.cancel();
		this.topUpdater = null;
		this.initialized = false;
	}
	
	@Override
	public boolean isInitialized() {
		return this.initialized != null;
	}
	
	@Override
	public boolean isDead() {
		return this.initialized == false;
	}
	
	@Override
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
	protected abstract @NotNull Portfel getPlugin();
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public abstract @Nullable TopEntry getByPos(int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public abstract @Nullable List<TopEntry> getFullTopCopy();
	

}
