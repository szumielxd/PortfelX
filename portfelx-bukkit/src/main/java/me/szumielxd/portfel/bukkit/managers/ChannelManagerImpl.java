package me.szumielxd.portfel.bukkit.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.enums.EcoType;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.managers.ChannelManager;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;
import me.szumielxd.portfel.bukkit.api.objects.Transaction;
import me.szumielxd.portfel.bukkit.managers.channel.InfoChannelManager;
import me.szumielxd.portfel.bukkit.managers.channel.SetupChannelManager;
import me.szumielxd.portfel.bukkit.managers.channel.SpecificChannelManager;
import me.szumielxd.portfel.bukkit.managers.channel.TransactionChannelManager;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;

public class ChannelManagerImpl implements ChannelManager {
	
	
	private final PortfelBukkitImpl plugin;
	
	private final @NotNull InfoChannelManager info;
	private final @NotNull SetupChannelManager setup;
	private final @NotNull TransactionChannelManager transactions;
	
	
	public ChannelManagerImpl(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.info = register(new InfoChannelManager(plugin));
		this.setup = register(new SetupChannelManager(plugin));
		this.transactions = register(new TransactionChannelManager(plugin));
	}

	@Override
	public @NotNull CompletableFuture<? extends User> requestPlayer(@NotNull Player player) {
		return this.info.requestPlayer(player);
	}


	@Override
	public @Nullable CompletableFuture<BalanceUpdateResult> requestGiveMinorBalance(@NotNull Player player, long amount) {
		return this.transactions.requestGiveMinorBalance(player, amount);
	}


	@Override
	public @Nullable CompletableFuture<BalanceUpdateResult> requestTakeMinorBalance(@NotNull Player player, long amount) {
		return this.transactions.requestTakeMinorBalance(player, amount);
	}


	@Override
	public void clearAwaitingUpdates(@NotNull Player player) {
		Stream.of(info, setup, transactions).forEach(ch -> ch.clearAwaitingUpdates(player));
	}


	@Override
	public @NotNull CompletableFuture<List<TopEntry>> requestTop(@NotNull Player player) {
		return this.info.requestTop(player, EcoType.MAIN);
	}


	@Override
	public @NotNull CompletableFuture<List<TopEntry>> requestMinorTop(@NotNull Player player) {
		return this.info.requestTop(player, EcoType.MINOR);
	}


	@Override
	public @Nullable CompletableFuture<Transaction> requestTransaction(@NotNull Player player, @NotNull OrderDataOnAir order) {
		return this.transactions.requestTransaction(player, order);
	}
	
	
	public void killManager() {
		Stream.of(info, setup, transactions).forEach(this::unregister);
	}
	
	
	public void setRegisterer(Consumer<BukkitOperableUser> registerer) {
		info.setRegisterer(registerer);
	}
	
	private <T extends SpecificChannelManager> T register(T manager) {
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, manager.getListenedChannel());
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, manager.getListenedChannel(), manager);
		return manager;
	}
	
	private <T extends SpecificChannelManager> T unregister(T manager) {
		plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, manager.getListenedChannel());
		plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, manager.getListenedChannel(), manager);
		return manager;
	}
	
	public static class UserResponseException extends RuntimeException {
		
		private static final long serialVersionUID = -7267879850550170783L;
		
		@Getter private final @NotNull Player player;
		@Getter private final @NotNull FailCause failCause;
		
		public UserResponseException(@NotNull Player player, @NotNull FailCause failCause) {
			super(failCause.message.formatted(player.getName()));
			this.player = player;
			this.failCause = failCause;
		}
		
		@RequiredArgsConstructor
		public enum FailCause {
			
			DISCONNECTED("User `%1$s` has been disconnected"),
			NOT_LOADED("User `%1$s` is not loaded");
			
			private final @NotNull String message;
			
		}
		
	}

}
