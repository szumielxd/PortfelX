package me.szumielxd.portfel.bungee.managers;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.objects.BungeeTaskWrapper;
import me.szumielxd.portfel.common.managers.TaskManager;
import me.szumielxd.portfel.common.objects.ExecutedTask;

public class BungeeTaskManager implements TaskManager {
	
	
	private final PortfelBungee plugin;
	
	
	public BungeeTaskManager(PortfelBungee plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Run task asynchronously.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTask(@NotNull Runnable task) {
		return this.runTaskAsynchronously(task);
	}
	
	/**
	 * Run task asynchronously.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTaskAsynchronously(@NotNull Runnable task) {
		return new BungeeTaskWrapper(this.plugin.getProxy().getScheduler().runAsync(this.plugin, task));
	}
	
	/**
	 * Run task asynchronously after specified delay.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTaskLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		return this.runTaskLaterAsynchronously(task, delay, unit);
	}
	
	/**
	 * Run task asynchronously after specified delay.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTaskLaterAsynchronously(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		return new BungeeTaskWrapper(this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, unit));
	}
	
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
	@Override
	public @NotNull ExecutedTask runTaskTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
		return this.runTaskTimerAsynchronously(task, delay, period, unit);
	}
	
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
	@Override
	public @NotNull ExecutedTask runTaskTimerAsynchronously(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
		return new BungeeTaskWrapper(this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, period, unit));
	}
	
	/**
	 * Cancel a task to prevent it from executing, or if its a repeating task,prevent its further execution.
	 * 
	 * @param id the id of the task to cancel
	 */
	@Override
	public void cancel(int id) {
		this.plugin.getProxy().getScheduler().cancel(id);
	}
	
	/**
	 * Cancel all tasks owned by this plugin, this preventing them from being
	 * executed.
	 */
	@Override
	public void cancelAll() {
		this.plugin.getProxy().getScheduler().cancel(this.plugin);
	}
	

}
