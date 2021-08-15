package me.szumielxd.portfel.bungee.managers;

import java.util.ArrayList;
import java.util.List;
import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.managers.TopManager;

public class BungeeTopManager extends TopManager {
	
	
	private final PortfelBungee plugin;
	private List<TopEntry> cachedTop;
	
	
	public BungeeTopManager(PortfelBungee plugin) {
		this.cachedTop = new ArrayList<>();
		this.plugin = plugin;
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected void update() {
		try {
			this.cachedTop = this.plugin.getDB().getTop(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get Portfel instance
	 * 
	 * @return plugin
	 */
	@Override
	protected Portfel getPlugin() {
		return this.plugin;
	}
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public TopEntry getByPos(int position) {
		try {
			return this.cachedTop.get(position-1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public List<TopEntry> getFullTopCopy() {
		return new ArrayList<>(this.cachedTop);
	}
	

}
