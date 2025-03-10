package me.szumielxd.portfel.bukkit.managers;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.BukkitTopManager;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.objects.BukkitImaginaryUser;
import me.szumielxd.portfel.common.managers.TopManagerImpl;
import me.szumielxd.portfel.common.utils.future.CompletableUtils;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class BukkitTopManagerImpl extends TopManagerImpl<Component> implements BukkitTopManager {
	
	
	@Getter private final @NotNull PortfelBukkitImpl plugin;
	private @NotNull Map<UUID, List<TopEntry>> cachedTop = new HashMap<>();
	private @NotNull Map<UUID, List<TopEntry>> cachedMinorTop = new HashMap<>();
	
	
	@Override
	public @NotNull BukkitTopManagerImpl init() {
		return (BukkitTopManagerImpl) super.init();
	}
	
	/**
	 * Update top.
	 */
	@Override
	protected @NotNull CompletableFuture<Void> update() {
		final ChannelManager channel = this.plugin.getChannelManager();
		Map<UUID, Player> distinctServers = this.plugin.getUserManager().getLoadedUsers().stream()
				.filter(User::isOnline)
				.filter(u -> !BukkitImaginaryUser.class.isInstance(u))
				.map(u -> new SimpleEntry<>(u.getRemoteId(), plugin.getServer().getPlayer(u.getUniqueId())))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (p, q) -> p));
		
		return CompletableFuture.allOf(
				updateMainTop(channel, distinctServers),
				updateMinorTop(channel, distinctServers));
	}
	
	private @NotNull CompletableFuture<Map<UUID, List<TopEntry>>> updateMainTop(@NotNull ChannelManager channel, @NotNull Map<UUID, Player> distinctServers) {
		return distinctServers.entrySet().parallelStream()
				.map(e -> Map.entry(e.getKey(), channel.requestTop(e.getValue())))
				.collect(CompletableUtils.mergedCompletableMap())
				.whenComplete(CompletableUtils.handleResult(
						map -> this.cachedTop = map,
						ex -> getPlugin().logger().warn(ex, "Couldn't update major eco top")));
	}
	
	private @NotNull CompletableFuture<Map<UUID, List<TopEntry>>> updateMinorTop(@NotNull ChannelManager channel, @NotNull Map<UUID, Player> distinctServers) {
		return distinctServers.entrySet().parallelStream()
				.map(e -> Map.entry(e.getKey(), channel.requestMinorTop(e.getValue())))
				.collect(CompletableUtils.mergedCompletableMap())
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
			return Collections.emptyList();
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
			return List.copyOf(this.cachedTop.get(proxyId));
		} catch (NullPointerException e) {
			return Collections.emptyList();
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
			return Collections.emptyList();
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
			return List.copyOf(this.cachedMinorTop.get(proxyId));
		} catch (NullPointerException e) {
			return Collections.emptyList();
		}
	}
	
	
	

}
