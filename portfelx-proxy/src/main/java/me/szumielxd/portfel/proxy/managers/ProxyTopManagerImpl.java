package me.szumielxd.portfel.proxy.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.common.managers.TopManagerImpl;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;

@RequiredArgsConstructor
public class ProxyTopManagerImpl extends TopManagerImpl implements ProxyTopManager {
	
	/**
	 * Portfel instance
	 */
	@Getter(AccessLevel.PROTECTED) @NonNull private final @NotNull PortfelProxyImpl plugin;
	private List<TopEntry> cachedTop = Collections.emptyList();
	private List<TopEntry> cachedMinorTop = Collections.emptyList();
	
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
			this.cachedMinorTop = this.plugin.getDatabase().getTop(this.getPlugin().getConfiguration().getInt(ProxyConfigKey.MAIN_TOP_SIZE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public @Nullable TopEntry getByPos(int position) {
		try {
			return this.cachedTop.get(position-1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public @Nullable TopEntry getByMinorPos(int position) {
		try {
			return this.cachedMinorTop.get(position-1);
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
	public @NotNull List<TopEntry> getFullTopCopy() {
		return new ArrayList<>(this.cachedTop);
	}
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public @NotNull List<TopEntry> getFullMinorTopCopy() {
		return new ArrayList<>(this.cachedMinorTop);
	}
	

}
