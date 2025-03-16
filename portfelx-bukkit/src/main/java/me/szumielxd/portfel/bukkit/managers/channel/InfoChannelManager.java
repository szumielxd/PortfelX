package me.szumielxd.portfel.bukkit.managers.channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.enums.EcoType;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl.UserResponseException;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl.UserResponseException.FailCause;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.info.ServerInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.UserInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.UserInfoRequestMessage;
import me.szumielxd.portfel.common.utils.CollectionUtils;
import me.szumielxd.portfel.common.utils.future.CompletableUtils;

public class InfoChannelManager extends SpecificChannelManager {
	
	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_INFO;

	private @Nullable Consumer<BukkitOperableUser> registerer = null;
	
	private final @NotNull Map<EcoType, Map<UUID, CompletableFuture<List<TopEntry>>>> awaitingTopUpdates = CollectionUtils.mapOfEachEnum(EcoType.class, v -> new ConcurrentHashMap<>()); // ecoType -> { remoteId -> future with list }
	private final @NotNull Map<EcoType, Map<UUID, CompletableFuture<List<TopEntry>>>> topUpdatesByUser = CollectionUtils.mapOfEachEnum(EcoType.class, v -> new ConcurrentHashMap<>()); // ecoType -> { userId -> remoteId }
	
	private final @NotNull Map<UUID, CompletableFuture<BukkitOperableUser>> awaitingUserUpdates = new HashMap<>();
	
	
	public InfoChannelManager(@NotNull PortfelBukkitImpl plugin) {
		super(plugin);
	}
	
	public void setRegisterer(Consumer<BukkitOperableUser> registerer) {
		if (this.registerer != null) {
			throw new RuntimeException("Registerer is set already.");
		}
		this.registerer = registerer;
	}

	@Override
	public void onPluginMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		getPlugin().debug("PluginMessage(user|%s): %s", player.getName(), subchannel);
		switch (subchannel) {
			case USER_INFO -> processUserInfoMessage(player, tag, subchannel, in);
			case TOP_INFO -> processTopMessage(player, tag, subchannel, in);
			default -> { /* nothing */}
		}
	}
	
	public void clearAwaitingUpdates(@NotNull Player player) {
		topUpdatesByUser.forEach((type, users) -> {
			Optional.ofNullable(users.remove(player.getUniqueId()))
					.ifPresent(future -> future.completeExceptionally(new UserResponseException(player, FailCause.DISCONNECTED)));
		});
		
	}
	
	/**
	 * Request top update from proxy the player belongs to.
	 * 
	 * @param player to determine proxy
	 * @return list of all top entries from given proxy (miscellaneous size)
	 * @throws Exception when something went wrong
	 */
	public @NotNull CompletableFuture<List<TopEntry>> requestTop(@NotNull Player player, @NotNull EcoType type) {
		User user = getPlugin().getUserManager().getUser(player.getUniqueId());
		if (user == null) {
			return CompletableFuture.failedFuture(new UserResponseException(player, FailCause.NOT_LOADED));
		}
		UUID uuid = user.getUniqueId();
		var topUpdateUsers = topUpdatesByUser.get(type);
		
		return CompletableUtils.fetchOrRun(awaitingTopUpdates.get(type),
				user.getRemoteId(),
				(k, future) -> {
					topUpdateUsers.put(uuid, future);
					sendTopRequest(player, type);
				}, (id, future) -> topUpdateUsers.remove(uuid, future));
	}
	
	/**
	 * Fetch actual wallet data of given player from proxy.
	 * 
	 * @param player the player
	 * @return {@link User} representation of given player
	 * @throws Exception when something went wrong
	 */
	public @NotNull CompletableFuture<BukkitOperableUser> requestPlayer(@NotNull Player player) {
		return CompletableUtils.fetchOrRun(awaitingUserUpdates,
				player.getUniqueId(),
				(k, future) -> sendUserRequest(player),
				(id, future) -> {});
	}
	
	
	private void sendServerId(@NotNull Player player) {
		User user = getPlugin().getUserManager().getUser(player.getUniqueId());
		if (isValidUser(user)) {
			var serverId = getPlugin().getIdentifierManager().getComplementary(user.getRemoteId());
			sendPluginMessage(player, new ServerInfoMessage(serverId));
		}
	}
	
	private void sendTopRequest(@NotNull Player player, @NotNull EcoType type) {
		sendPluginMessage(player, new TopRequestMessage(type));
	}
	
	private void sendUserRequest(@NotNull Player player) {
		sendPluginMessage(player, new UserInfoRequestMessage());
	}
	
	private void processUserInfoMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var response = decode(UserInfoMessage.class, tag, subchannel, in);
		if (isValidProxy(response.getProxyId())) {
			BukkitOperableUser user = Optional.ofNullable(getPlugin().getUserManager().getUser(response.getUser().getUniqueId()))
					.filter(BukkitOperableUser.class::isInstance)
					.map(BukkitOperableUser.class::cast)
					.orElse(null);
			if (user != null) {
				user.updateInfo(response);
			} else {
				user = BukkitOperableUser.buildFromUserInfoMessage(getPlugin(), response);
			}
			registerer.accept(user);
			var future = awaitingUserUpdates.get(user.getUniqueId());
			if (future != null) {
				future.complete(user);
			}
			sendServerId(player);
		}
	}
	
	private void processTopMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var response = decode(TopMessage.class, tag, subchannel, in);
		if (isValidProxy(response.getProxyId())) {
			var top = Stream.of(response.getEntries())
					.map(u -> new TopEntry(u.getUser().getUniqueId(), u.getUser().getUsername(), u.getBalance()))
					.toList();
			topUpdatesByUser.get(response.getType()).remove(player.getUniqueId(), response.getProxyId());
			var future = awaitingTopUpdates.get(response.getType()).get(response.getProxyId());
			if (future != null) {
				future.complete(top);
			}
		}
	}

}
