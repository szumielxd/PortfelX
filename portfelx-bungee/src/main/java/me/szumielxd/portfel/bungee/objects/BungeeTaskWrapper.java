package me.szumielxd.portfel.bungee.objects;

import me.szumielxd.portfel.common.objects.ExecutedTask;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeTaskWrapper implements ExecutedTask {
	
	
	private final ScheduledTask task;
	
	
	public BungeeTaskWrapper(ScheduledTask task) {
		this.task = task;
	}
	
	
	/**
	 * Cancel this task.
	 */
	@Override
	public void cancel() {
		this.task.cancel();
	}
	
	/**
	 * Check whether this task is running in main thread.
	 * 
	 * @return true if task is running in main thread, false otherwise
	 */
	@Override
	public boolean isSync() {
		return false;
	}
	
	/**
	 * Get identifier of this task.
	 * 
	 * @return ID of task
	 */
	@Override
	public int getId() {
		return this.task.getId();
	}
	

}
