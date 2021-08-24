package me.szumielxd.portfel.bungee.managers;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.configuration.BungeeConfigKey;
import me.szumielxd.portfel.bungee.api.objects.BungeeSenderWrapper;
import me.szumielxd.portfel.bungee.objects.PrizeToken;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.CryptoUtils;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class TokenManager {
	
	
	private final PortfelBungeeImpl plugin;
	private final Set<UUID> pendingTokenRequests;
	
	
	public TokenManager(PortfelBungeeImpl plugin) {
		this.plugin = plugin;
		this.pendingTokenRequests = new HashSet<>(this.plugin.getConfiguration().getInt(BungeeConfigKey.TOKEN_MANAGER_POOLSIZE));
	}
	
	
	public void tryValidateToken(@NotNull ProxiedPlayer target, @NotNull String token) {
		CommonPlayer sender = (CommonPlayer) BungeeSenderWrapper.get(target);
		User user = this.plugin.getUserManager().getUser(target.getUniqueId());
		if (user == null) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_USER_NOT_LOADED.component(DARK_RED)));
		}
		if (this.pendingTokenRequests.size() >= this.plugin.getConfiguration().getInt(BungeeConfigKey.TOKEN_MANAGER_POOLSIZE)) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_FULLPOOL.component(RED)));
			return;
		}
		if (!this.pendingTokenRequests.add(target.getUniqueId())) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_ALREADY.component(RED)));
			return;
		}
		try {
			this.executeValidation(target, user, sender, token);
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(DARK_RED)));
		}
		this.pendingTokenRequests.remove(target.getUniqueId());
	}
	
	
	private void executeValidation(@NotNull ProxiedPlayer target, @NotNull User user, @NotNull CommonPlayer sender, @NotNull String token) throws Exception {
		PrizeToken prize = this.plugin.getTokenDB().getToken(token);
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
				if (this.plugin.getTokenDB().destroyToken(token)) {
					this.plugin.getDBLogger().logTokenUse(user, user.getServerName(), prize);
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
	
	
	
	
	
	private void sendTokenPrizeExecution(@NotNull ProxiedPlayer player, @NotNull UUID serverId, @NotNull String token, @NotNull String order, long globalOrdersCount) {
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
		Server srv = player.getServer();
		if (srv.isConnected()) srv.sendData(Portfel.CHANNEL_TRANSACTIONS, out.toByteArray());
	}
	

}
