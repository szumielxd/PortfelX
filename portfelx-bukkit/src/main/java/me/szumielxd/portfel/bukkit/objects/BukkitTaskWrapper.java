package me.szumielxd.portfel.bukkit.objects;

import org.bukkit.scheduler.BukkitTask;

import me.szumielxd.portfel.api.objects.ExecutedTask;

public class BukkitTaskWrapper implements ExecutedTask {
	
	
	private final BukkitTask task;
	
	
	public BukkitTaskWrapper(BukkitTask task) {
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
		return this.task.isSync();
	}
	
	/**
	 * Get identifier of this task.
	 * 
	 * @return ID of task
	 */
	@Override
	public int getId() {
		return this.task.getTaskId();
	}
	

}
