package me.szumielxd.portfel.proxy.listeners;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.PacketCoder;
import me.szumielxd.portfel.common.communication.coders.messages.common.UserIdentifier;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.ServerInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.UserInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage.TopUser;
import me.szumielxd.portfel.common.utils.CryptoUtils.DecryptionException;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.PluginMessageTarget;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

@RequiredArgsConstructor
public abstract class ChannelListener<T extends PortfelProxyImpl<C>, C> {
	
	
	@Getter(AccessLevel.PROTECTED) private final @NotNull T plugin;
	private final @NotNull Gson gson = new GsonBuilder()
			.disableHtmlEscaping()
			.setPrettyPrinting()
			.create();
	
	
	protected final boolean isListendChannel(@Nullable String tag) {
		return Portfel.CHANNEL_SETUP.equals(tag)
				|| Portfel.CHANNEL_TRANSACTIONS.equals(tag)
				|| Portfel.CHANNEL_USERS.equals(tag);
	}
	
	
	@SuppressWarnings("unchecked")
	protected final Optional<Boolean> onPluginMessage(@NotNull PluginMessageTarget sender, @NotNull PluginMessageTarget target, @NotNull String tag, byte[] message) {
		if (sender instanceof ProxyServerConnection server && target instanceof ProxyPlayer<?>) {
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			ProxyPlayer<C> player = (ProxyPlayer<C>) target;
			if (Portfel.CHANNEL_USERS.equals(tag)) {
				return switch (subchannel) {
					case "UserInfo" -> Optional.of(this.onUserData(server, player, tag, subchannel, in));
					case "ServerInfo" -> Optional.of(this.onServerData(server, player, tag, subchannel, in));
					case "Top" -> Optional.of(this.onTopData(server, player, tag, subchannel, in));
					default -> Optional.of(true);
				};
			} else if (Portfel.CHANNEL_TRANSACTIONS.equals(tag)) {
				return switch (subchannel) {
					case "Buy" -> Optional.of(this.onTransaction(server, player, tag, subchannel, in));
					case "MinorTake" -> Optional.of(this.onMinorEcoTake(server, player, tag, subchannel, in));
					case "MinorGive" -> Optional.of(this.onMinorEcoGive(server, player, tag, subchannel, in));
					default -> Optional.of(true);
				};
			}
		}
		return Optional.of(true);
	}
	
	
	// user channel
	private boolean onUserData(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		this.plugin.getTaskManager().runTaskAsynchronously(() -> {
			try {
				User user = this.plugin.getUserManager().getOrCreateUser(target.getUniqueId());
				var msg = new UserInfoMessage(
						this.plugin.getProxyId(),
						new UserIdentifier(
								target.getUniqueId(),
								target.getName()),
						user.getBalance(),
						user.getMinorBalance(),
						user.isDeniedInTop());
				sender.sendPluginMessage(tag, msg.toBytePacket());
			} catch (Exception e) {	
				plugin.logger().severe(e, "An exception occurred while processing UserInfo response");
			}
		});
		return true;
	}
	
	
	// user channel
	private boolean onServerData(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
			ProxyOperableUser user = this.plugin.getUserManager().getUser(target.getUniqueId());
			if (user != null) {
				PacketCoder.decode(in, ServerInfoMessage.class).ifPresent(serverInfo -> {
					if (canAccess(serverInfo.getServerId())) {
						user.setRemoteIdAndName(serverInfo.getServerId(), serverInfo.getServerName());
					}
				});
			}
		} catch (Exception e) {	
			plugin.logger().severe(e, "An exception occurred while processing ServerInfo response");
		}
		return true;
	}
	
	
	// user channel
	private boolean onTopData(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		try {
			PacketCoder.decode(in, TopRequestMessage.class).ifPresentOrElse(request -> {
				var type = request.getType();
				var players = switch (type) {
					case MAIN -> this.plugin.getTopManager().getFullTopCopy();
					case MINOR -> this.plugin.getTopManager().getFullMinorTopCopy();
				};
				var playersTop = players.stream()
						.map(e -> new TopUser(
								new UserIdentifier(e.getUniqueId(), e.getName()),
								e.getBalance()))
						.toArray(TopUser[]::new);
				sender.sendPluginMessage(tag, new TopMessage(this.plugin.getProxyId(), type, playersTop).toBytePacket());
			}, () -> { throw new IllegalArgumentException("Unknown packet structure for subchannel `%s` in channel `%s`"
					.formatted(subchannel, tag)); });
		} catch (Exception e) {	
			this.plugin.logger().severe(e, "An exception occurred while processing Top request");
		}
		return true;
	}
	
	
	// transaction channel
	private boolean onTransaction(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		PacketCoder.decode(in, TransactionRequestMessage.class).ifPresent(request -> {
			var serverId = request.getServerId();
			if (canAccess(request.getServerId())) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> runTransaction(
								data.getTransactionId(),
								serverId,
								sender,
								target,
								ProxyActionExecutor.plugin(data.getPlugin()),
								data.getOrder(),
								plugin.getAccessManager().getServerName(serverId),
								data.getValue()));
			}
		});
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoGive(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		PacketCoder.decode(in, MinorEcoGiveRequestMessage.class).ifPresent(request -> {
			var serverId = request.getServerId();
			if (canAccess(request.getServerId(), "minorbalance:give")) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> giveMinorEconomy(sender, data.getTransactionId(), serverId, target, data.getValue()));
			}
		});
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoTake(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull String subchannel, @NotNull ByteArrayDataInput in) {
		PacketCoder.decode(in, MinorEcoTakeRequestMessage.class).ifPresent(request -> {
			var serverId = request.getServerId();
			if (canAccess(request.getServerId(), "minorbalance:take")) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> takeMinorEconomy(sender, data.getTransactionId(), serverId, target, data.getValue()));
			}
		});
		return true;
	}
	
	private void giveMinorEconomy(@NotNull ProxyServerConnection<C> sender, @NotNull String transactionId, @NotNull UUID serverId, @NotNull ProxyPlayer<C> target, long value) {
		ProxyOperableUser user = this.plugin.getUserManager().getUser(target.getUniqueId());
		Optional.ofNullable(user)
				.map(u -> u.giveMinorBalance(value))
				.orElse(CompletableFuture.completedFuture(null))
				.whenComplete((res, ex) -> {
					TransactionStatus status = user != null ? TransactionStatus.OK :
						ex != null ? TransactionStatus.ERROR : TransactionStatus.NOT_LOADED;
					String error = ex != null ? ex.getMessage() : "";
					var packet = new MinorEcoGiveResultMessage(
							this.plugin.getProxyId(),
							new EncryptedObject<>(
									MinorEcoGiveResultMessage.CryptoPayload.class,
									new MinorEcoGiveResultMessage.CryptoPayload(
											transactionId,
											user.getMinorBalance(),
											status,
											error),
									getCryptoKey(serverId)));
					sender.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, packet.toBytePacket());
				});
	}
	
	private void takeMinorEconomy(@NotNull ProxyServerConnection<C> sender, @NotNull String transactionId, @NotNull UUID serverId, @NotNull ProxyPlayer<C> target, long value) {
		ProxyOperableUser user = this.plugin.getUserManager().getUser(target.getUniqueId());
		Optional.ofNullable(user)
				.map(u -> u.takeMinorBalance(value))
				.orElse(CompletableFuture.completedFuture(null))
				.whenComplete((res, ex) -> {
					TransactionStatus status = user != null ? TransactionStatus.OK :
						ex != null ? TransactionStatus.ERROR : TransactionStatus.NOT_LOADED;
					String error = ex != null ? ex.getMessage() : "";
					var packet = new MinorEcoTakeResultMessage(
							this.plugin.getProxyId(),
							new EncryptedObject<>(
									MinorEcoTakeResultMessage.CryptoPayload.class,
									new MinorEcoTakeResultMessage.CryptoPayload(
											transactionId,
											user.getMinorBalance(),
											status,
											error),
									getCryptoKey(serverId)));
					sender.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, packet.toBytePacket());
				});
	}
	
	
	private void runTransaction(@NotNull String transactionId, @NotNull UUID serverId, @NotNull ProxyServerConnection<C> srv, @NotNull ProxyPlayer<C> target, @NotNull ActionExecutor pluginExecutor, @NotNull String order, @NotNull String server, long value) {
		User user = this.plugin.getUserManager().getUser(target.getUniqueId());
		user.takeBalance(value, pluginExecutor, server, order).whenComplete((res, ex) -> {
			TransactionStatus status = TransactionStatus.OK;
			String error = "";
			int globalOrders = 0;
			if (ex != null) {
				status = TransactionStatus.ERROR;
				error = ex.getMessage();
			} else {
				globalOrders = (int) this.plugin.getOrdersManager().getOrders().values().stream()
						.filter(o -> canAccess(serverId, "order:" + o.getName()))
						.filter(o -> o.examine(user, order))
						.count();
			}
			var packet = new TransactionResultMessage(
					this.plugin.getProxyId(),
					new EncryptedObject<>(
							TransactionResultMessage.CryptoPayload.class,
							new TransactionResultMessage.CryptoPayload(
									transactionId,
									user.getBalance(),
									status,
									globalOrders,
									error),
							getCryptoKey(serverId)));
			srv.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, packet.toBytePacket());
		});
	}
	
	
	private boolean canAccess(@NotNull UUID serverId) {
		return plugin.getAccessManager().canAccess(serverId);
	}
	
	
	private boolean canAccess(@NotNull UUID serverId, @NotNull String permission) {
		return plugin.getAccessManager().canAccess(serverId, permission);
	}
	
	
	private @NotNull String getCryptoKey(@NotNull UUID serverId) {
		return Objects.requireNonNull(this.plugin.getAccessManager().getHashKey(serverId), "Invalid server");
	}
	
	private @NotNull <E> Optional<E> decrypt(@NotNull EncryptedObject<E> obj, @NotNull UUID serverId, @NotNull ProxyPlayer<C> player, @NotNull String subchannel, @NotNull String tag) {
		try {
			return Optional.of(obj.decrypt(getCryptoKey(serverId)));
		} catch (DecryptionException e) {
			plugin.logger().warn(e, "Couldn't decrypt data for player `%s` in subchannel `%s` in channel `%s`"
					.formatted(player.getName(), subchannel, tag));
			return Optional.empty();
		}
	}
	

}
