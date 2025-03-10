package me.szumielxd.portfel.bukkit.managers.channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager.BalanceUpdateResult;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;
import me.szumielxd.portfel.bukkit.api.objects.Transaction.TransactionResult;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl.UserResponseException;
import me.szumielxd.portfel.bukkit.managers.ChannelManagerImpl.UserResponseException.FailCause;
import me.szumielxd.portfel.bukkit.objects.BukkitImaginaryUser;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.bukkit.objects.TransactionImpl;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TokenTransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionResultMessage;
import me.szumielxd.portfel.common.utils.future.CompletableUtils;

public class TransactionChannelManager extends SpecificChannelManager {
	
	private static final @NotNull DateTimeFormatter LOG_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");;
	
	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_TRANSACTIONS;
	
	private final Map<UUID, TransactionImpl> waitingTransactions = new HashMap<>();
	private final Map<UUID, CompletableFuture<BalanceUpdateResult>> waitingMinorEcoGive = new HashMap<>();
	private final Map<UUID, CompletableFuture<BalanceUpdateResult>> waitingMinorEcoTake = new HashMap<>();
	
	public TransactionChannelManager(@NotNull PortfelBukkitImpl plugin) {
		super(plugin);
	}

	@Override
	public void onPluginMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		getPlugin().debug("PluginMessage(user|%s): %s", player.getName(), subchannel);
		switch (subchannel) {
			case BUY -> processBuyMessage(player, tag, subchannel, in);
			case MINORECO_GIVE -> processMinorGiveMessage(player, tag, subchannel, in);
			case MINORECO_TAKE -> processMinorTakeMessage(player, tag, subchannel, in);
			case TOKEN -> processTokenMessage(player, tag, subchannel, in);
			default -> { /* nothing */}
		}
	}
	
	@Override
	public void clearAwaitingUpdates(@NotNull Player player) {
		Stream.of(waitingTransactions, waitingMinorEcoGive, waitingMinorEcoTake)
				.map(map -> map.get(player.getUniqueId()))
				.filter(Objects::nonNull)
				.forEach(future -> future.completeExceptionally(new UserResponseException(player, FailCause.DISCONNECTED)));
	}
	
	
	public @NotNull CompletableFuture<BalanceUpdateResult> requestGiveMinorBalance(@NotNull Player player, long amount) {
		var transactionId = UUID.randomUUID();
		var user = getPlugin().getUserManager().getUser(player.getUniqueId());
		if (!isValidUser(user)) {
			return CompletableFuture.failedFuture(new UserResponseException(player, FailCause.NOT_LOADED));
		}
		var future = CompletableUtils.insertAutoremovable(waitingMinorEcoGive, transactionId);
		sendMinorEcoGive(player, user, transactionId, amount);
		return future;
	}

	
	public @NotNull CompletableFuture<BalanceUpdateResult> requestTakeMinorBalance(@NotNull Player player, long amount) {
		var transactionId = UUID.randomUUID();
		var user = getPlugin().getUserManager().getUser(player.getUniqueId());
		if (!isValidUser(user)) {
			return CompletableFuture.failedFuture(new UserResponseException(player, FailCause.NOT_LOADED));
		}
		var future = CompletableUtils.insertAutoremovable(waitingMinorEcoTake, transactionId);
		sendMinorEcoTake(player, user, transactionId, amount);
		return future;
	}
	
	
	public @NotNull CompletableFuture<Transaction> requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order) {
		var transactionId = UUID.randomUUID();
		return getPlugin().getUserManager().getOrLoadUser(player.getUniqueId())
				.thenApply(u -> {
					if (u instanceof BukkitOperableUser user) {
						if (user.inTestmode()) {
							BukkitLangKey.MAIN_WARNING
									.draft(BukkitLangKey.TESTMODE_NOTIFICATION)
									.send(BukkitSender.wrap(getPlugin(), player), true);
							return TransactionImpl.completedDummy(getPlugin(), user, transactionId, order);
						} else if (!(user instanceof BukkitImaginaryUser)) {  // check if user is correctly loaded
							var trans = CompletableUtils.insertAutoremovable(waitingTransactions, transactionId, new TransactionImpl(getPlugin(), user, transactionId, order));
							sendTransaction(player, trans);
							return trans;
						}
					}
					return null;
				});
	}
	
	
	private void processBuyMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(TransactionResultMessage.class, tag, subchannel, in);
		if (isValidProxy(request.getProxyId())) {
			decrypt(request.getData(), getCryptoKey(), BukkitSender.player(getPlugin(), player), subchannel, tag).ifPresent(data -> {
				var result = new TransactionResult(
						data.getTransactionId(),
						data.getStatus(),
						data.getNewBalance(),
						data.getGlobalOrders(),
						new RuntimeException(data.getError()));
				var transaction = waitingTransactions.get(data.getTransactionId());
				if (transaction != null && transaction.getUser().getRemoteId().equals(request.getProxyId())) {
					getPlugin().getTaskManager().runTaskAsynchronously(() -> transaction.finish(result));
				}
			});
		}
	}
	
	private void processMinorGiveMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(MinorEcoGiveResultMessage.class, tag, subchannel, in);
		if (isValidProxy(request.getProxyId())) {
			decrypt(request.getData(), getCryptoKey(), BukkitSender.player(getPlugin(), player), subchannel, tag).ifPresent(data -> {
				var future = waitingMinorEcoGive.get(data.getTransactionId());
				if (future != null) {
					future.complete(new BalanceUpdateResult(data.getStatus(), data.getNewBalance()));
				}
			});
		}
	}
	
	private void processMinorTakeMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(MinorEcoTakeResultMessage.class, tag, subchannel, in);
		if (isValidProxy(request.getProxyId())) {
			decrypt(request.getData(), getCryptoKey(), BukkitSender.player(getPlugin(), player), subchannel, tag).ifPresent(data -> {
				var future = waitingMinorEcoTake.get(data.getTransactionId());
				if (future != null) {
					future.complete(new BalanceUpdateResult(data.getStatus(), data.getNewBalance()));
				}
			});
		}
	}
	
	private void processTokenMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(TokenTransactionRequestMessage.class, tag, subchannel, in);
		if (isValidProxy(request.getProxyId())) {
			decrypt(request.getData(), getCryptoKey(), BukkitSender.player(getPlugin(), player), subchannel, tag).ifPresent(data -> {
				if (isValidServer(request.getProxyId(), data.getServerId())) {
					User user = getPlugin().getUserManager().getUser(player.getUniqueId());
					if (user != null && user.getRemoteId().equals(request.getProxyId())) {
						long executed = getPlugin().getPrizesManager().getOrders().values().stream()
								.filter(o -> o.examine(user, data.getOrder(), data.getToken()))
								.count();
						getPlugin().getTaskManager().runTaskAsynchronously(
								() -> logTokenPrize("Handled token %s (%s) for %s(%s). Result: %d global, %d locale orders"
										.formatted(data.getToken(), data.getOrder(), user.getName(), user.getUniqueId(), data.getGlobalOrders(), executed)));
					}
				}
			});
		}
	}
	
	private void sendTransaction(@NotNull Player player, @NotNull Transaction transaction) {
		sendPluginMessage(player, new TransactionRequestMessage(
				getServerId(transaction.getUser().getRemoteId()),
				new EncryptedObject<>(
						TransactionRequestMessage.CryptoPayload.class,
						new TransactionRequestMessage.CryptoPayload(
								transaction.getTransactionId(),
								transaction.getOrder().getPrice(),
								getPlugin().getName(),
								transaction.getOrder().getOrderName()),
						getCryptoKey())));
	}
	
	private void sendMinorEcoGive(@NotNull Player player, @NotNull User user, @NotNull UUID transactionId, @NotNull long amount) {
		sendPluginMessage(player, new MinorEcoGiveRequestMessage(
				getServerId(user.getRemoteId()),
				new EncryptedObject<>(
						MinorEcoGiveRequestMessage.CryptoPayload.class,
						new MinorEcoGiveRequestMessage.CryptoPayload(
								transactionId,
								amount),
						getCryptoKey())));
	}
	
	private void sendMinorEcoTake(@NotNull Player player, @NotNull User user, @NotNull UUID transactionId, @NotNull long amount) {
		sendPluginMessage(player, new MinorEcoTakeRequestMessage(
				getServerId(user.getRemoteId()),
				new EncryptedObject<>(
						MinorEcoTakeRequestMessage.CryptoPayload.class,
						new MinorEcoTakeRequestMessage.CryptoPayload(
								transactionId,
								amount),
						getCryptoKey())));
	}
	
	private void logTokenPrize(@NotNull String text) {
		try {
			Path path = getPlugin().getDataDirectory().resolve("token-prize.log");
			if (!Files.exists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}
			Files.writeString(path, "[%s] %s%n".formatted(LOG_DATETIME_FORMAT.format(LocalTime.now()), text), StandardOpenOption.APPEND);
		} catch (IOException e) {
			getPlugin().logger().severe(e, "Cannot append token logs to file");
		}
	}

}
