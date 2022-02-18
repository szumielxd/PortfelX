package me.szumielxd.portfel.bukkit.managers;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitTaskWrapper;

public class BukkitTaskManagerImpl implements TaskManager {
	
	
	private final PortfelBukkitImpl plugin;
	
	
	public BukkitTaskManagerImpl(PortfelBukkitImpl plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Run task.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTask(@NotNull Runnable task) {
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTask(this.plugin.asPlugin(), task));
	}
	
	/**
	 * Run task asynchronously.
	 * 
	 * @param task the task to be run
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTaskAsynchronously(@NotNull Runnable task) {
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin.asPlugin(), task));
	}
	
	/**
	 * Run task after specified delay.
	 * 
	 * @param task the task to be run
	 * @param delay the delay before this task will be executed
	 * @param unit the unit in which the delay will be measured
	 * @return the scheduled task
	 */
	@Override
	public @NotNull ExecutedTask runTaskLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTaskLater(this.plugin.asPlugin(), task, unit.toMillis(delay)/50));
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
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin.asPlugin(), task, unit.toMillis(delay)/50));
	}
	
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
	@Override
	public @NotNull ExecutedTask runTaskTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTaskTimer(this.plugin.asPlugin(), task, unit.toMillis(delay)/50, unit.toMillis(period)/50));
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
		return new BukkitTaskWrapper(this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin.asPlugin(), task, unit.toMillis(delay)/50, unit.toMillis(period)/50));
	}
	
	/**
	 * Cancel a task to prevent it from executing, or if its a repeating task,prevent its further execution.
	 * 
	 * @param id the id of the task to cancel
	 */
	@Override
	public void cancel(int id) {
		this.plugin.getServer().getScheduler().cancelTask(id);
	}
	
	/**
	 * Cancel all tasks owned by this plugin, this preventing them from being
	 * executed.
	 */
	@Override
	public void cancelAll() {
		this.plugin.getServer().getScheduler().cancelTasks(this.plugin.asPlugin());
	}
	

}
