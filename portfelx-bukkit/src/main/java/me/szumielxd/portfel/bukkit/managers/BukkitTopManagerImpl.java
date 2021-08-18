package me.szumielxd.portfel.bukkit.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.BukkitTopManager;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.managers.TopManagerImpl;

public class BukkitTopManagerImpl extends TopManagerImpl implements BukkitTopManager {
	
	
	private final PortfelBukkitImpl plugin;
	private Map<UUID, List<TopEntry>> cachedTop;
	
	
	public BukkitTopManagerImpl(PortfelBukkitImpl plugin) {
		this.cachedTop = new HashMap<>();
		this.plugin = plugin;
	}
	
	@Override
	public @NotNull BukkitTopManagerImpl init() {
		return (BukkitTopManagerImpl) super.init();
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected void update() {
		try {
			final ChannelManager channel = this.plugin.getChannelManager();
			this.cachedTop = this.plugin.getUserManager().getLoadedUsers().stream().map(BukkitOperableUser.class::cast).filter(User::isOnline)
					.collect(Collectors.toMap(u -> u.getProxyId(), Function.identity(), (p, q) -> p)).values().parallelStream()
					.collect(Collectors.toMap(u -> u.getProxyId(), u -> {
						try{
							Player player = this.plugin.getServer().getPlayer(u.getUniqueId());
							return channel.requestTop(player);
						} catch(Exception e) {
							e.printStackTrace();
							return new ArrayList<>();
						}}, (p, q) -> p));
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
	protected @NotNull Portfel getPlugin() {
		return this.plugin;
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
			return this.getByPos(this.cachedTop.keySet().iterator().next(), position);
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param proxyId target proxy
	 * @param position position to obtain
	 * @return top entry
	 */
	public @Nullable TopEntry getByPos(@Nullable UUID proxyId, int position) {
		try {
			return this.cachedTop.get(proxyId).get(position-1);
		} catch (IndexOutOfBoundsException|NoSuchElementException|NullPointerException e) {
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
		try {
			return this.getFullTopCopy(this.cachedTop.keySet().iterator().next());
		} catch (NoSuchElementException e) {
			return null;
		}
		
	}
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached top
	 */
	public @Nullable List<TopEntry> getFullTopCopy(@Nullable UUID proxyId) {
		try {
			return new ArrayList<>(this.cachedTop.get(proxyId));
		} catch (NullPointerException e) {
			return null;
		}
	}
	

}
