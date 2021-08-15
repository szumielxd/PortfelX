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

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.managers.TopManager;
import me.szumielxd.portfel.common.objects.User;

public class BukkitTopManager extends TopManager {
	
	
	private final PortfelBukkit plugin;
	private Map<UUID, List<TopEntry>> cachedTop;
	
	
	public BukkitTopManager(PortfelBukkit plugin) {
		this.cachedTop = new HashMap<>();
		this.plugin = plugin;
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
	public TopEntry getByPos(UUID proxyId, int position) {
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
	public List<TopEntry> getFullTopCopy() {
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
	public List<TopEntry> getFullTopCopy(UUID proxyId) {
		try {
			return new ArrayList<>(this.cachedTop.get(proxyId));
		} catch (NullPointerException e) {
			return null;
		}
	}
	

}
