package me.szumielxd.portfel.proxy.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.common.managers.TopManagerImpl;
import me.szumielxd.portfel.common.utils.future.CompletableUtils;
import me.szumielxd.portfel.common.utils.future.ExceptionalSupplier;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;

@RequiredArgsConstructor
public class ProxyTopManagerImpl<C> extends TopManagerImpl<C> implements ProxyTopManager {
	
	/**
	 * Portfel instance
	 */
	@Getter(AccessLevel.PROTECTED) @NonNull private final @NotNull PortfelProxyImpl<C> plugin;
	
	private List<TopEntry> cachedTop = Collections.emptyList();
	private List<TopEntry> cachedMinorTop = Collections.emptyList();
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	@Override
	public @NotNull ProxyTopManagerImpl<C> init() {
		super.init();
		return this;
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected CompletableFuture<Void> update() {
		int size = getPlugin().getConfiguration().getInt(ProxyConfigKey.MAIN_TOP_SIZE);
		return CompletableFuture.allOf(
				updateMainTop(size),
				updateMinorTop(size));
	}
	
	private CompletableFuture<List<TopEntry>> updateMainTop(int size) {
		return ExceptionalSupplier.supplyAsync(() -> plugin.getDatabase().getTop(size))
				.whenComplete(CompletableUtils.handleResult(
						map -> this.cachedTop = map,
						ex -> getPlugin().logger().warn(ex, "Couldn't update major eco top")));
	}
	
	private CompletableFuture<List<TopEntry>> updateMinorTop(int size) {
		return ExceptionalSupplier.supplyAsync(() -> plugin.getDatabase().getMinorTop(size))
				.whenComplete(CompletableUtils.handleResult(
						map -> this.cachedMinorTop = map,
						ex -> getPlugin().logger().warn(ex, "Couldn't update minor eco top")));
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
