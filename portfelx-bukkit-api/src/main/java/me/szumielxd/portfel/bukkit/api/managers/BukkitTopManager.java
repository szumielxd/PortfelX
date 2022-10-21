package me.szumielxd.portfel.bukkit.api.managers;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.managers.TopManager;

public interface BukkitTopManager extends TopManager {
	
	
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
	public abstract @NotNull List<TopEntry> getFullTopCopy();
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached top
	 */
	public abstract @NotNull List<TopEntry> getFullTopCopy(@Nullable UUID proxyId);
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return minor top entry
	 */
	@Override
	public @Nullable TopEntry getByMinorPos(int position);
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param proxyId target proxy
	 * @param position position to obtain
	 * @return minor top entry
	 */
	public @Nullable TopEntry getByMinorPos(@Nullable UUID proxyId, int position);
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @return copy of actually cached minor top
	 */
	@Override
	public @NotNull List<TopEntry> getFullMinorTopCopy();
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached minor top
	 */
	public @NotNull List<TopEntry> getFullMinorTopCopy(@Nullable UUID proxyId);
	

}
