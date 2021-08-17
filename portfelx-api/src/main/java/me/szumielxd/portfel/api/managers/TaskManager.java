package me.szumielxd.portfel.api.managers;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.ExecutedTask;

public interface TaskManager {
	
	
	/**
	 * Run task.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTask(@NotNull Runnable task);
	
	/**
	 * Run task asynchronously.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTaskAsynchronously(@NotNull Runnable task);
	
	/**
	 * Run task after specified delay.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTaskLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);
	
	/**
	 * Run task asynchronously after specified delay.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTaskLaterAsynchronously(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);
	
	/**
	 * Run task after specified delay.
	 * The scheduled task will continue running at the specified interval.
	 * The interval will not begin to count down until the last task invocation is complete.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param period the interval before subsequent executions of this task
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTaskTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);
	
	/**
	 * Run task asynchronously after specified delay.
	 * The scheduled task will continue running at the specified interval.
	 * The interval will not begin to count down until the last task invocation is complete.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param period the interval before subsequent executions of this task
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	public @NotNull ExecutedTask runTaskTimerAsynchronously(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);
	
	/**
	 * Cancel a task to prevent it from executing, or if its a repeating task,prevent its further execution.
	 * 
	 * @param id the id of the task to cancel
	 */
	public void cancel(int id);
	
	/**
	 * Cancel all tasks owned by this plugin, this preventing them from being
	 * executed.
	 */
	public void cancelAll();
	

}
