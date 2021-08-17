package me.szumielxd.portfel.api.objects;

public interface ExecutedTask {
	
	
	/**
	 * Cancel this task.
	 */
	public void cancel();
	
	/**
	 * Check whether this task is running in main thread.
	 * 
	 * @return true if task is running in main thread, false otherwise
	 */
	public boolean isSync();
	
	/**
	 * Get identifier of this task.
	 * 
	 * @return ID of task
	 */
	public int getId();
	

}
