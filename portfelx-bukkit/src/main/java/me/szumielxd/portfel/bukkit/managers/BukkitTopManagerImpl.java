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
import me.szumielxd.portfel.common.managers.TopManagerImpl;

public class BukkitTopManagerImpl extends TopManagerImpl implements BukkitTopManager {
	
	
	private final PortfelBukkitImpl plugin;
	private Map<UUID, List<TopEntry>> cachedTop;
	private Map<UUID, List<TopEntry>> cachedMinorTop;
	
	
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
			this.cachedTop = this.plugin.getUserManager().getLoadedUsers().stream().filter(User::isOnline)
					.collect(Collectors.toMap(User::getRemoteId, Function.identity(), (p, q) -> p)).values().parallelStream()
					.collect(Collectors.toMap(User::getRemoteId, u -> {
						try{
							Player player = this.plugin.getServer().getPlayer(u.getUniqueId());
							return channel.requestTop(player);
						} catch(Exception e) {
							e.printStackTrace();
							return new ArrayList<>();
						}}, (p, q) -> p));
			this.cachedMinorTop = this.plugin.getUserManager().getLoadedUsers().stream().filter(User::isOnline)
					.collect(Collectors.toMap(User::getRemoteId, Function.identity(), (p, q) -> p)).values().parallelStream()
					.collect(Collectors.toMap(User::getRemoteId, u -> {
						try{
							Player player = this.plugin.getServer().getPlayer(u.getUniqueId());
							return channel.requestMinorTop(player);
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
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param proxyId target proxy
	 * @param position position to obtain
	 * @return top entry
	 */
	@Override
	public @Nullable TopEntry getByPos(@Nullable UUID proxyId, int position) {
		try {
			return this.cachedTop.get(proxyId).get(position-1);
		} catch (IndexOutOfBoundsException|NoSuchElementException|NullPointerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	@Override
	public @NotNull List<TopEntry> getFullTopCopy() {
		try {
			return this.getFullTopCopy(this.cachedTop.keySet().iterator().next());
		} catch (NoSuchElementException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached top
	 */
	@Override
	public @NotNull List<TopEntry> getFullTopCopy(@Nullable UUID proxyId) {
		try {
			return new ArrayList<>(this.cachedTop.get(proxyId));
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return minor top entry
	 */
	@Override
	public @Nullable TopEntry getByMinorPos(int position) {
		try {
			return this.getByMinorPos(this.cachedMinorTop.keySet().iterator().next(), position);
		} catch (NoSuchElementException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param proxyId target proxy
	 * @param position position to obtain
	 * @return minor top entry
	 */
	@Override
	public @Nullable TopEntry getByMinorPos(@Nullable UUID proxyId, int position) {
		try {
			return this.cachedMinorTop.get(proxyId).get(position-1);
		} catch (IndexOutOfBoundsException|NoSuchElementException|NullPointerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @return copy of actually cached minor top
	 */
	@Override
	public @NotNull List<TopEntry> getFullMinorTopCopy() {
		try {
			return this.getFullMinorTopCopy(this.cachedMinorTop.keySet().iterator().next());
		} catch (NoSuchElementException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @param proxyId target proxy
	 * @return copy of actually cached minor top
	 */
	@Override
	public @NotNull List<TopEntry> getFullMinorTopCopy(@Nullable UUID proxyId) {
		try {
			return new ArrayList<>(this.cachedMinorTop.get(proxyId));
		} catch (NullPointerException e) {
			throw new RuntimeException(e);
		}
	}
	

}
