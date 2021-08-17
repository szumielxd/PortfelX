package me.szumielxd.portfel.bukkit.api.managers;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.managers.TopManagerImpl;

public abstract class BukkitTopManager extends TopManagerImpl {
	
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public @NotNull BukkitTopManager init() {
		return (BukkitTopManager) super.init();
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected abstract void update();
	
	/**
	 * Get Portfel instance
	 * 
	 * @return plugin
	 */
	@Override
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
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param proxyId target proxy
	 * @param position position to obtain
	 * @return top entry
	 */
	public abstract @Nullable TopEntry getByPos(@Nullable UUID proxyId, int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public abstract @Nullable List<TopEntry> getFullTopCopy();
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached top
	 */
	public abstract @Nullable List<TopEntry> getFullTopCopy(@Nullable UUID proxyId);
	

}
