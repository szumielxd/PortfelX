package me.szumielxd.portfel.bukkit.objects;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;
import me.szumielxd.portfel.bukkit.utils.PlaceholderUtils;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

public class TransactionImpl extends CompletableFuture<Transaction.TransactionResult> implements Transaction {
		
	
	private final PortfelBukkitImpl plugin;
	private final BukkitOperableUser user;
	private final UUID transactionId;
	private final OrderDataOnAir order;
	
	public TransactionImpl(@NotNull PortfelBukkitImpl plugin, @NotNull BukkitOperableUser user, @NotNull UUID transactionId, @NotNull OrderDataOnAir order) {
		this.plugin = plugin;
		this.user = user;
		this.transactionId = transactionId;
		this.order = order;
		this.complete(getResult());
	}
	
	
	/**
	 * get target of this transaction.
	 * 
	 * @return user related with this transaction
	 */
	@Override
	public @NotNull BukkitOperableUser getUser() {
		return this.user;
	}
	
	/**
	 * Get identifier of this transaction.
	 * 
	 * @return transaction's UUID
	 */
	@Override
	public @NotNull UUID getTransactionId() {
		return this.transactionId;
	}
	
	/**
	 * Get order related with this transaction.
	 * 
	 * @return prepared order
	 */
	@Override
	public @NotNull OrderDataOnAir getOrder() {
		return this.order;
	}
	
	/**
	 * Get result of this transaction.
	 * 
	 * @return result if transaction is finished, otherwise null
	 */
	@Override
	public @Nullable TransactionResult getResult() {
		return getNow(null);
	}
	
	@Override
	public void obtrudeValue(@Nullable TransactionResult result) {
		super.obtrudeValue(result);
		long oldBalance = user.getBalance();
		user.setPlainBalance(result.getNewBalance());
		
		if (result.getStatus() == TransactionStatus.OK) {
			var player = Bukkit.getPlayer(user.getUniqueId());
			var plainReplacements = Map.of("order", order.getOrderName());
			var replacements = plainReplacements.entrySet().stream()
					.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> MessageDraft.plain(e.getValue())));
			
			// broadcast
			getOrder().getActions().broadcasts().stream()
					.map(msg -> PlaceholderUtils.getDraft(msg, player, user, replacements))
					.forEach(msg -> msg.send(plugin.getCommonServer()));
	
			// message
			var wrapped = BukkitSender.player(plugin, player);
			getOrder().getActions().messages().stream()
					.map(msg -> PlaceholderUtils.getDraft(msg, player, user, replacements))
					.forEach(msg -> msg.send(wrapped));
			
			// command
			var console = plugin.getServer().getConsoleSender();
			plugin.getTaskManager().runTask(() -> getOrder().getActions().commands().stream()
					.map(cmd -> cmd.startsWith("/") ? cmd.substring(1) : cmd)
					.map(cmd -> PlaceholderUtils.getPlain(cmd, player, user, plainReplacements))
					.forEach(cmd -> plugin.getServer().dispatchCommand(console, cmd)));
			
			String ip = Optional.ofNullable(player.getAddress())
					.map(InetSocketAddress::getAddress)
					.map(InetAddress::getHostAddress)
					.orElse("offline");
			
			log("%s(%s) successfully bought `%s` for %s$. Old balance: %s$, new balance: %s$"
					.formatted(user.getName(), ip, order.getOrderName(), order.getPrice(), oldBalance, user.getBalance()));
		}
	}
	
	/**
	 * Finish this transaction.
	 * 
	 * @implNote <b>Thread unsafe</b>
	 * @param result of this transaction
	 * @return true if transaction was successfully finished, otherwise false
	 * @throws RuntimeException if executed in main thread
	 */
	@Override
	public boolean finish(@NotNull TransactionResult result) throws RuntimeException {
		Objects.requireNonNull(result, "result cannot be null");
		if (plugin.getServer().isPrimaryThread()) throw new RuntimeException("Transaction cannot be finished in main thread.");
		return complete(result);
	}
	
	
	private void log(@NotNull String text) {
		Objects.requireNonNull(text, "text cannot be null");
		Path f = this.plugin.getDataDirectory().resolve("transactions.log");
		try {
			if (!Files.exists(f.getParent())) Files.createDirectories(f.getParent());
			text = String.format("[%s] %s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), text);
			if (!Files.exists(f)) Files.createFile(f);
			Files.write(f, Collections.singletonList(text), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static TransactionImpl completed(@NotNull PortfelBukkitImpl plugin, @NotNull BukkitOperableUser user, @NotNull UUID transactionId, @NotNull OrderDataOnAir order, @NotNull TransactionResult result) {
		var t = new TransactionImpl(plugin, user, transactionId, order);
		t.complete(result);
		return t;
	}
	
	
	public static TransactionImpl completedDummy(@NotNull PortfelBukkitImpl plugin, @NotNull BukkitOperableUser user, @NotNull UUID transactionId, @NotNull OrderDataOnAir order) {
		return completed(plugin, user, transactionId, order,
				new TransactionResult(transactionId, TransactionStatus.OK, user.getBalance(), 0, null));
	}
	

}
