package me.szumielxd.portfel.proxy.api.managers;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.managers.TopManager;

public interface ProxyTopManager extends TopManager {
	
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public @Nullable TopEntry getByPos(int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public @Nullable List<TopEntry> getFullTopCopy();
	

}
