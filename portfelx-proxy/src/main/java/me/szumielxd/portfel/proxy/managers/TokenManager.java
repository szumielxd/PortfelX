package me.szumielxd.portfel.proxy.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TokenTransactionRequestMessage;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import me.szumielxd.portfel.proxy.objects.ProxyOperableUser;

public class TokenManager<C> {
	
	
	private final @NotNull PortfelProxyImpl<C> plugin;
	private final @NotNull Set<UUID> pendingTokenRequests;
	private @NotNull List<PrizeToken> cachedTokens = new ArrayList<>();
	private @Nullable ExecutedTask tokenCacheUpdater;
	
	
	public TokenManager(@NotNull PortfelProxyImpl<C> plugin) {
		this.plugin = plugin;
		this.pendingTokenRequests = new HashSet<>(this.plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE));
	}
	
	
	public @NotNull TokenManager<C> init() {
		this.tokenCacheUpdater = plugin.getTaskManager().runTaskTimerAsynchronously(this::updateTokens, 0L, 1L, TimeUnit.MINUTES);
		return this;
	}
	
	
	public void tryValidateToken(@NotNull ProxyPlayer<C> target, @NotNull String token) {
		var user = plugin.getUserManager().getUser(target.getUniqueId());
		if (user == null) {
			MainLangKey.ERROR_COMMAND_USER_NOT_LOADED.draft()
					.sendPrefixed(target);
			return;
		}
		if (pendingTokenRequests.size() >= plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE)) {
			ProxyLangKey.TOKEN_CHECK_FULLPOOL.draft()
					.sendPrefixed(target);
			return;
		}
		if (!pendingTokenRequests.add(target.getUniqueId())) {
			ProxyLangKey.TOKEN_CHECK_ALREADY.draft()
					.sendPrefixed(target);
			return;
		}
		try {
			executeValidation(target, user, target, token);
		} catch (Exception e) {
			plugin.logger().severe(e, "Cannot validate token `%s` for player `%s`", token, target.getName());
			MainLangKey.ERROR_COMMAND_EXECUTION.draft()
					.sendPrefixed(target);
		}
		pendingTokenRequests.remove(target.getUniqueId());
	}
	
	
	public @NotNull List<PrizeToken> getCachedTokens() {
		return Collections.unmodifiableList(cachedTokens);
	}
	
	
	public @NotNull CompletableFuture<Boolean> deleteToken(@NotNull String token) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (plugin.getTokenDatabase().destroyToken(token)) {
					cachedTokens.removeIf(t -> token.equals(t.getToken()));
					return true;
				}
			} catch (Exception e) {
				plugin.logger().severe(e, "Cannot delete token with if `%s`", token);
			}
			return false;
		});
	}
	
	
	public void killManager() {
		if (tokenCacheUpdater != null) {
			tokenCacheUpdater.cancel();
		}
		tokenCacheUpdater = null;
	}
	
	
	private void updateTokens() {
		try {
			cachedTokens = new ArrayList<>(this.plugin.getTokenDatabase().getTokens(null, null, null, null, null));
		} catch (Exception e) {
			plugin.logger().warn(e, "Cannot update cached tokens");
		}
	}
	
	
	private void executeValidation(@NotNull ProxyPlayer<C> target, @NotNull ProxyOperableUser user, @NotNull CommonPlayer<C> sender, @NotNull String token) throws Exception {
		PrizeToken prize = this.plugin.getTokenDatabase().getToken(token);
		if (prize != null) {
			String serverName = user.getRemoteName();
			if (validateTokenUsage(target, user, sender, prize) && plugin.getTokenDatabase().destroyToken(token)) {
				cachedTokens.removeIf(t -> token.equals(t.getToken()));
				plugin.getTransactionLogger().logTokenUse(user, serverName != null ? serverName : "UNKNOWN", prize);
				long executed = this.plugin.getPrizesManager().getOrders().values().stream()
						.filter(o -> o.examine(user, prize.getOrder(), token))
						.count();
				if (user.getRemoteId() != null) {
					sendTokenPrizeExecution(target, user.getRemoteId(), token, prize.getOrder(), executed);
				}
			}
		} else {
			ProxyLangKey.TOKEN_CHECK_INVALID.draft()
					.sendPrefixed(sender);
		}
	}
	
	
	private boolean validateTokenUsage(@NotNull ProxyPlayer<C> target, @NotNull ProxyOperableUser user, @NotNull CommonPlayer<C> sender, @NotNull PrizeToken prize) {
		boolean valid = switch (prize.getSelectorType()) {
			case ANY -> true;
			case REGISTERED -> plugin.getAccessManager().canAccess(user.getRemoteId());
			case WHITELIST -> prize.getServerNames().contains(user.getRemoteName());
		};
		if (!valid) {
			var message = switch (prize.getSelectorType()) {
					case REGISTERED -> ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_REGISTERED.draft();
					case WHITELIST -> ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_WHITELIST.draft(
							prize.getServerNames().stream()
									.map(ProxyLangKey.TOKEN_CHECK_SERVER_INVALID_WHITELIST_SERVER_FORMAT::draft)
									.collect(MessageDraft.joinFlattened(", ")));
					default -> null;
			};
			if (message != null) {
				message.sendPrefixed(sender);
			}
		}
		return valid;
	}
	
	
	
	
	
	private void sendTokenPrizeExecution(@NotNull ProxyPlayer<C> player, @NotNull UUID serverId, @NotNull String token, @NotNull String order, long globalOrdersCount) {
		player.getServer().ifPresent(server -> {
			var packet = new TokenTransactionRequestMessage(
					plugin.getProxyId(),
					new EncryptedObject<>(
							TokenTransactionRequestMessage.CryptoPayload.class,
							new TokenTransactionRequestMessage.CryptoPayload(
									serverId,
									token,
									order,
									(int) globalOrdersCount),
							plugin.getAccessManager().getHashKey(serverId)));
			server.sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, packet.toBytePacket());
		});
	}
	

}
