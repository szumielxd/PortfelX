package me.szumielxd.portfel.proxy.managers;

import static net.kyori.adventure.text.format.NamedTextColor.*;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.ExecutedTask;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.configuration.ProxyConfigKey;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import net.kyori.adventure.text.Component;

public class TokenManager {
	
	
	private final PortfelProxyImpl plugin;
	private final Set<UUID> pendingTokenRequests;
	private List<PrizeToken> cachedTokens = new ArrayList<>();
	private ExecutedTask tokenCacheUpdater;
	
	
	public TokenManager(PortfelProxyImpl plugin) {
		this.plugin = plugin;
		this.pendingTokenRequests = new HashSet<>(this.plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE));
	}
	
	
	public TokenManager init() {
		this.tokenCacheUpdater = this.plugin.getTaskManager().runTaskTimerAsynchronously(this::updateTokens, 0L, 1L, TimeUnit.MINUTES);
		return this;
	}
	
	
	public void tryValidateToken(@NotNull ProxyPlayer target, @NotNull String token) {
		User user = this.plugin.getUserManager().getUser(target.getUniqueId());
		if (user == null) {
			target.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_USER_NOT_LOADED.component(DARK_RED)));
		}
		if (this.pendingTokenRequests.size() >= this.plugin.getConfiguration().getInt(ProxyConfigKey.TOKEN_MANAGER_POOLSIZE)) {
			target.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_FULLPOOL.component(RED)));
			return;
		}
		if (!this.pendingTokenRequests.add(target.getUniqueId())) {
			target.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_ALREADY.component(RED)));
			return;
		}
		try {
			this.executeValidation(target, user, target, token);
		} catch (Exception e) {
			e.printStackTrace();
			target.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(DARK_RED)));
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
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				// silence
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
	
	
	private void executeValidation(@NotNull ProxyPlayer target, @NotNull User user, @NotNull CommonPlayer sender, @NotNull String token) throws Exception {
		PrizeToken prize = this.plugin.getTokenDatabase().getToken(token);
		if (prize != null) {
			boolean valid = false;
			switch (prize.getSelectorType()) {
				case ANY:
					valid = true;
					break;
				case REGISTERED:
					if (user.getServerName() != null) valid = true;
					break;
				case WHITELIST:
					if (prize.getServerNames().contains(user.getServerName())) valid = true;
					break;
			}
			if (valid) {
				if (this.plugin.getTokenDatabase().destroyToken(token)) {
					this.cachedTokens.removeIf(t -> token.equals(t.getToken()));
					this.plugin.getTransactionLogger().logTokenUse(user, user.getServerName(), prize);
					long executed = this.plugin.getPrizesManager().getOrders().values().stream().filter(o -> o.examine(user, prize.getOrder(), token)).count();
					if (user.getRemoteId() != null) this.sendTokenPrizeExecution(target, user.getRemoteId(), token, prize.getOrder(), executed);
					return;
				}
			} else {
				switch (prize.getSelectorType()) {
				case REGISTERED:
					sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_SERVER_INVALID_REGISTERED.component(RED)));
					return;
				case WHITELIST:
					sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_SERVER_INVALID_WHITELIST.component(RED, MiscUtils.join(Component.text(", ", RED), prize.getServerNames().stream().map(srv -> Component.text(srv, AQUA)).collect(Collectors.toList())))));
					return;
				default:
					return;
				}
			}
		}
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_INVALID.component(RED)));
	}
	
	
	
	
	
	private void sendTokenPrizeExecution(@NotNull ProxyPlayer player, @NotNull UUID serverId, @NotNull String token, @NotNull String order, long globalOrdersCount) {
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
		Optional<ProxyServerConnection> srv = player.getServer();
		if (srv.isPresent()) srv.get().sendPluginMessage(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	

}
