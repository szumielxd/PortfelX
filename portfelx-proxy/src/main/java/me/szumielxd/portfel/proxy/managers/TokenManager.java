package me.szumielxd.portfel.proxy.managers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public class TokenManager<C> {
	
	
	private final PortfelProxyImpl<C> plugin;
	private final Set<UUID> pendingTokenRequests;
	private List<PrizeToken> cachedTokens = new ArrayList<>();
	private ExecutedTask tokenCacheUpdater;
	
	
	public TokenManager(PortfelProxyImpl<C> plugin) {
		this.plugin = plugin;
		this.pendingTokenRequests = new HashSet<>(this.plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE));
	}
	
	
	public TokenManager<C> init() {
		this.tokenCacheUpdater = this.plugin.getTaskManager().runTaskTimerAsynchronously(this::updateTokens, 0L, 1L, TimeUnit.MINUTES);
		return this;
	}
	
	
	public void tryValidateToken(@NotNull ProxyPlayer<C> target, @NotNull String token) {
		var user = this.plugin.getUserManager().getUser(target.getUniqueId());
		if (user == null) {
			MainLangKey.ERROR_COMMAND_USER_NOT_LOADED.draft()
					.sendPrefixed(target);
			return;
		}
		if (this.pendingTokenRequests.size() >= this.plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE)) {
			ProxyLangKey.TOKEN_CHECK_FULLPOOL.draft()
					.sendPrefixed(target);
			return;
		}
		if (!this.pendingTokenRequests.add(target.getUniqueId())) {
			ProxyLangKey.TOKEN_CHECK_ALREADY.draft()
					.sendPrefixed(target);
			return;
		}
		try {
			executeValidation(target, user, target, token);
		} catch (Exception e) {
			e.printStackTrace();
			MainLangKey.ERROR_COMMAND_EXECUTION.draft()
					.sendPrefixed(target);
		}
		this.pendingTokenRequests.remove(target.getUniqueId());
	}
	
	
	public List<PrizeToken> getCachedTokens() {
		return Collections.unmodifiableList(this.cachedTokens);
	}
	
	
	public CompletableFuture<Boolean> deleteToken(@NotNull String token) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (this.plugin.getTokenDatabase().destroyToken(token)) {
					this.cachedTokens.removeIf(t -> token.equals(t.getToken()));
					return true;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (TimeoutException e) {
				// silent
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		});
	}
	
	
	public void killManager() {
		if (this.tokenCacheUpdater != null) this.tokenCacheUpdater.cancel();
		this.tokenCacheUpdater = null;
	}
	
	
	private void updateTokens() {
		try {
			this.cachedTokens = new ArrayList<>(this.plugin.getTokenDatabase().getTokens(null, null, null, null, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void executeValidation(@NotNull ProxyPlayer<C> target, @NotNull ProxyOperableUser user, @NotNull CommonPlayer<C> sender, @NotNull String token) throws Exception {
		PrizeToken prize = this.plugin.getTokenDatabase().getToken(token);
		if (prize != null) {
			String serverName = user.getRemoteName();
			boolean valid = switch (prize.getSelectorType()) {
				case ANY -> true;
				case REGISTERED -> plugin.getAccessManager().canAccess(user.getRemoteId());
				case WHITELIST -> prize.getServerNames().contains(serverName);
			};
			if (valid) {
				if (this.plugin.getTokenDatabase().destroyToken(token)) {
					this.cachedTokens.removeIf(t -> token.equals(t.getToken()));
					this.plugin.getTransactionLogger().logTokenUse(user, serverName != null ? serverName : "UNKNOWN", prize);
					long executed = this.plugin.getPrizesManager().getOrders().values().stream()
							.filter(o -> o.examine(user, prize.getOrder(), token))
							.count();
					if (user.getRemoteId() != null) {
						this.sendTokenPrizeExecution(target, user.getRemoteId(), token, prize.getOrder(), executed);
					}
					return;
				}
			} else {
				switch (prize.getSelectorType()) {
						case REGISTERED -> ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_REGISTERED.draft()
								.sendPrefixed(sender);
						case WHITELIST -> ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_WHITELIST.draft(
								prize.getServerNames().stream()
										.map(ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_WHITELIST_SERVER_FORMAT::draft)
										.collect(MessageDraft.joinFlattened(", ")))
								.sendPrefixed(sender);
						default -> {
							// empty
						}
				}
				return;
			}
		}
		ProxyLangKey.TOKEN_CHECK_INVALID.draft()
				.sendPrefixed(sender);
	}
	
	
	
	
	
	private void sendTokenPrizeExecution(@NotNull ProxyPlayer<C> player, @NotNull UUID serverId, @NotNull String token, @NotNull String order, long globalOrdersCount) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Token"); // subchannel
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(bout);) {
			dout.writeUTF(this.plugin.getProxyId().toString()); // proxyId
			dout.writeUTF(serverId.toString()); // serverId
			dout.writeUTF(token); // token
			dout.writeUTF(order); // orderName
			dout.writeLong(globalOrdersCount); // globalOrdersCount
			CryptoUtils.encodeBytesToOutput(out, bout.toByteArray(), this.plugin.getAccessManager().getHashKey(serverId));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Optional<ProxyServerConnection<C>> srv = player.getServer();
		if (srv.isPresent()) srv.get().sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	

}
