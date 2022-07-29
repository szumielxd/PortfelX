package me.szumielxd.portfel.bukkit.objects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.audience.Audience;

public class TransactionImpl implements Transaction {
		
	
	private final PortfelBukkitImpl plugin;
	private final User user;
	private final UUID transactionId;
	private final OrderDataOnAir order;
	
	private TransactionResult result = null;
	
	public TransactionImpl(@NotNull PortfelBukkitImpl plugin, @NotNull User user, @NotNull UUID transactionId, @NotNull OrderDataOnAir order) {
		this.plugin = plugin;
		this.user = user;
		this.transactionId = transactionId;
		this.order = order;
	}
	
	
	/**
	 * get target of this transaction.
	 * 
	 * @return user related with this transaction
	 */
	@Override
	public @NotNull User getUser() {
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
		return this.result;
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
		if (this.plugin.getServer().isPrimaryThread()) throw new RuntimeException("Transaction cannot be finished in main thread.");
		if (this.result != null) return false;
		if (!this.transactionId.equals(result.getTransactionId())) return false;
		this.result = result;
		
		long oldBalance = this.user.getBalance();
		
		((BukkitOperableUser)this.user).setPlainBalance(this.result.getNewBalance());
		
		if (!this.result.getStatus().equals(TransactionStatus.OK)) return true;
		
		// replacements: %player% %playerId%
		Pattern pattern = Pattern.compile("%((player(Id)?)|(order))%", Pattern.CASE_INSENSITIVE);
		Function<MatchResult, String> replacer = match -> {
			if (match.group().equalsIgnoreCase("%order%")) return this.order.getName();
			if (match.group().equalsIgnoreCase("%player%")) return this.user.getName(); // %player%
			return this.user.getUniqueId().toString(); // %playerId%
		};
		
		// broadcast
		Audience all = this.plugin.getServer() instanceof Audience ? this.plugin.getServer() : this.plugin.adventure().all();
		this.getOrder().getBroadcast().forEach(msg -> all.sendMessage(MiscUtils.parseComponent(msg, pattern, replacer)));
		
		// message
		Audience player = Audience.class.isAssignableFrom(Player.class) ? Bukkit.getPlayer(user.getUniqueId()) : this.plugin.adventure().player(user.getUniqueId());
		this.getOrder().getMessage().forEach(msg -> player.sendMessage(MiscUtils.parseComponent(msg, pattern, replacer)));
		
		// command
		this.plugin.getTaskManager().runTask(() -> this.getOrder().getCommand().forEach(cmd -> {
			if (cmd.startsWith("/")) cmd = cmd.substring(1, cmd.length());
			this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtils.replaceAll(pattern.matcher(cmd), replacer));
		}));
		
		OfflinePlayer target = Bukkit.getOfflinePlayer(user.getUniqueId());
		String ip = target.isOnline()? target.getPlayer().getAddress().getAddress().getHostAddress() : "offline";
		this.log(String.format("%s(%s) successfully bought `%s` for %s$. Old balance: %s$, new balance: %s$", user.getName(), ip, order.getName(), order.getPrice(), oldBalance, user.getBalance()));
		
		return true; 
	}
	
	
	private void log(@NotNull String text) {
		Objects.requireNonNull(text, "text cannot be null");
		Path f = this.plugin.getDataFolder().resolve("transactions.log");
		try {
			if (!Files.exists(f.getParent())) Files.createDirectories(f.getParent());
			text = String.format("[%s] %s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), text);
			if (!Files.exists(f)) Files.createFile(f);
			Files.write(f, Collections.singletonList(text), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
