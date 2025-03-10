package me.szumielxd.portfel.proxy.listeners.channel;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionResultMessage;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;

public class TransactionsChannelListener<T extends PortfelProxyImpl<C>, C> extends SpecificChannelListener<T, C> {

	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_TRANSACTIONS;
	
	public TransactionsChannelListener(@NotNull T plugin) {
		super(plugin);
	}


	@Override
	public void onPluginMessage(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		switch (subchannel) {
			case BUY -> onTransaction(sender, target, tag, subchannel, in);
			case MINORECO_TAKE -> onMinorEcoTake(sender, target, tag, subchannel, in);
			case MINORECO_GIVE -> onMinorEcoGive(sender, target, tag, subchannel, in);
			default -> { /* ignore */}
		}
	}
	

	// transaction channel
	private boolean onTransaction(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(TransactionRequestMessage.class, tag, subchannel, in);
		try {
			var serverId = request.getServerId();
			if (canAccess(request.getServerId())) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> runTransaction(data.getTransactionId(), serverId, sender, target, data.getPlugin(), data.getOrder(), data.getValue()));
			}
		} catch (Exception e) {	
			getPlugin().logger().severe(e, "An exception occurred while processing Transaction request");
		}
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoGive(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		try {
			var request = decode(MinorEcoGiveRequestMessage.class, tag, subchannel, in);
			var serverId = request.getServerId();
			if (canAccess(request.getServerId(), "minorbalance:give")) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> giveMinorEconomy(sender, data.getTransactionId(), serverId, target, data.getValue()));
			}
		} catch (Exception e) {	
			getPlugin().logger().severe(e, "An exception occurred while processing MinorEco Give request");
		}
		return true;
	}
	
	// transaction channel
	private boolean onMinorEcoTake(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		try {
			var request = decode(MinorEcoTakeRequestMessage.class, tag, subchannel, in);
			var serverId = request.getServerId();
			if (canAccess(request.getServerId(), "minorbalance:take")) {
				decrypt(request.getData(), serverId, target, subchannel, tag)
						.filter(data -> data.getValue() > 0)
						.ifPresent(data -> takeMinorEconomy(sender, data.getTransactionId(), serverId, target, data.getValue()));
			}
		} catch (Exception e) {	
			getPlugin().logger().severe(e, "An exception occurred while processing MinorEco Take request");
		}
		return true;
	}
	
	private void giveMinorEconomy(@NotNull ProxyServerConnection<C> sender, @NotNull UUID transactionId, @NotNull UUID serverId, @NotNull ProxyPlayer<C> target, long value) {
		var user = getPlugin().getUserManager().getUser(target.getUniqueId());
		Optional.ofNullable(user)
				.map(u -> u.giveMinorBalance(value))
				.orElse(CompletableFuture.completedFuture(null))
				.whenComplete((res, ex) -> {
					TransactionStatus status = TransactionStatus.wrap(user, ex);
					String error = ex != null ? ex.getMessage() : "";
					var packet = new MinorEcoGiveResultMessage(
							getPlugin().getProxyId(),
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
	
	private void takeMinorEconomy(@NotNull ProxyServerConnection<C> sender, @NotNull UUID transactionId, @NotNull UUID serverId, @NotNull ProxyPlayer<C> target, long value) {
		var user = getPlugin().getUserManager().getUser(target.getUniqueId());
		Optional.ofNullable(user)
				.map(u -> u.takeMinorBalance(value))
				.orElse(CompletableFuture.completedFuture(null))
				.whenComplete((res, ex) -> {
					TransactionStatus status = TransactionStatus.wrap(user, ex);
					String error = ex != null ? ex.getMessage() : "";
					var packet = new MinorEcoTakeResultMessage(
							getPlugin().getProxyId(),
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
	
	
	private void runTransaction(@NotNull UUID transactionId, @NotNull UUID serverId, @NotNull ProxyServerConnection<C> srv, @NotNull ProxyPlayer<C> target, @NotNull String pluginName, @NotNull String order, long value) {
		var user = getPlugin().getUserManager().getUser(target.getUniqueId());
		var server = getPlugin().getAccessManager().getServerName(serverId);
		var pluginExecutor = ProxyActionExecutor.plugin(pluginName);
		Optional.ofNullable(user)
				.map(u -> u.takeBalance(value, pluginExecutor, server, order))
				.orElse(CompletableFuture.completedFuture(null))
				.whenComplete((res, ex) -> {
					TransactionStatus status = TransactionStatus.wrap(user, ex);
					String error = ex != null ? ex.getMessage() : "";
					int globalOrders = 0;
					if (status == TransactionStatus.OK) {
						globalOrders = (int) getPlugin().getOrdersManager().getOrders().values().stream()
									.filter(o -> canAccess(serverId, "order:" + o.getName()))
									.filter(o -> o.examine(user, order))
									.count();
					}
					var packet = new TransactionResultMessage(
							getPlugin().getProxyId(),
							new EncryptedObject<>(
									TransactionResultMessage.CryptoPayload.class,
									new TransactionResultMessage.CryptoPayload(
											transactionId,
											user != null ? user.getBalance() : 0,
											status,
											globalOrders,
											error),
									getCryptoKey(serverId)));
					srv.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, packet.toBytePacket());
				});
	}

}
