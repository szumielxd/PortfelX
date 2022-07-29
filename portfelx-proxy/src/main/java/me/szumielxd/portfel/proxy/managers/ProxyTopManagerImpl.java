package me.szumielxd.portfel.proxy.managers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.managers.TopManagerImpl;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;

public class ProxyTopManagerImpl extends TopManagerImpl implements ProxyTopManager {
	
	
	private final PortfelProxyImpl plugin;
	private List<TopEntry> cachedTop;
	
	
	public ProxyTopManagerImpl(@NotNull PortfelProxyImpl plugin) {
		this.cachedTop = new ArrayList<>();
		this.plugin = plugin;
	}
	
	@Override
	public @NotNull ProxyTopManagerImpl init() {
		return (ProxyTopManagerImpl) super.init();
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected void update() {
		try {
			this.cachedTop = this.plugin.getDatabase().getTop(this.getPlugin().getConfiguration().getInt(ProxyConfigKey.MAIN_TOP_SIZE));
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
