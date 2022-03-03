package me.szumielxd.portfel.proxy.managers;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class ProxyTaskManagerImpl implements TaskManager {
	
	
	private final PortfelProxyImpl plugin;
	
	
	public ProxyTaskManagerImpl(PortfelProxyImpl plugin) {
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
		return this.plugin.getCommonServer().getScheduler().runTask(task);
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
		return this.plugin.getCommonServer().getScheduler().runTaskLaterAsynchronously(task, delay, unit);
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
		return this.plugin.getCommonServer().getScheduler().runTaskTimer(task, delay, period, unit);
	}
	
	/**
	 * Cancel a task to prevent it from executing, or if its a repeating task,prevent its further execution.
	 * 
	 * @param id the id of the task to cancel
	 */
	@Override
	public void cancel(int id) {
		this.plugin.getCommonServer().getScheduler().cancel(id);
	}
	
	/**
	 * Cancel all tasks owned by this plugin, this preventing them from being
	 * executed.
	 */
	@Override
	public void cancelAll() {
		this.plugin.getCommonServer().getScheduler().cancelAll();
	}
	

}
