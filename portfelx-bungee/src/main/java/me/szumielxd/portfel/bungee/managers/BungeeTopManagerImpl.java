package me.szumielxd.portfel.bungee.managers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.configuration.BungeeConfigKey;
import me.szumielxd.portfel.bungee.api.managers.BungeeTopManager;
import me.szumielxd.portfel.common.managers.TopManagerImpl;

public class BungeeTopManagerImpl extends TopManagerImpl implements BungeeTopManager {
	
	
	private final PortfelBungeeImpl plugin;
	private List<TopEntry> cachedTop;
	
	
	public BungeeTopManagerImpl(@NotNull PortfelBungeeImpl plugin) {
		this.cachedTop = new ArrayList<>();
		this.plugin = plugin;
	}
	
	@Override
	public @NotNull BungeeTopManagerImpl init() {
		return (BungeeTopManagerImpl) super.init();
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected void update() {
		try {
			this.cachedTop = this.plugin.getDB().getTop(this.getPlugin().getConfiguration().getInt(BungeeConfigKey.MAIN_TOP_SIZE));
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
	public @Nullable List<TopEntry> getFullTopCopy() {
		return new ArrayList<>(this.cachedTop);
	}
	

}
