package me.szumielxd.portfel.proxy.listeners.channel;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.common.UserIdentifier;
import me.szumielxd.portfel.common.communication.coders.messages.info.ServerInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage.TopUser;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopRequestMessage;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;

public class InfoChannelListener<T extends PortfelProxyImpl<C>, C> extends SpecificChannelListener<T, C> {

	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_INFO;
	
	public InfoChannelListener(@NotNull T plugin) {
		super(plugin);
	}


	@Override
	public void onPluginMessage(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		switch (subchannel) {
			case USER_INFO -> onUserData(sender, target, tag);
			case SERVER_INFO -> onServerData(target, tag, subchannel, in);
			case TOP_INFO -> onTopData(sender, tag, subchannel, in);
			default -> { /* ignore */}
		}
	}
	
	
	// user channel
	private boolean onUserData(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag) {
		getPlugin().getTaskManager().runTaskAsynchronously(() -> {
			try {
				var user = getPlugin().getUserManager().getOrCreateUser(target.getUniqueId(), target.getName());
				sender.sendPluginMessage(tag, user.buildInfoPacket().toBytePacket());
			} catch (Exception e) {	
				getPlugin().logger().severe(e, "An exception occurred while processing UserInfo response");
			}
		});
		return true;
	}
	
	
	// user channel
	private boolean onServerData(@NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		try {
			Optional.ofNullable(getPlugin().getUserManager().getUser(target.getUniqueId())).ifPresent(user -> {
				var serverInfo = decode(ServerInfoMessage.class, tag, subchannel, in);
				if (canAccess(serverInfo.getServerId())) {
					user.setRemoteId(serverInfo.getServerId());
				}
			});
		} catch (Exception e) {	
			getPlugin().logger().severe(e, "An exception occurred while processing ServerInfo response");
		}
		return true;
	}
	
	
	// user channel
	private boolean onTopData(@NotNull ProxyServerConnection<C> sender, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		try {
			var request = decode(TopRequestMessage.class, tag, subchannel, in);
			var type = request.getType();
			var players = switch (type) {
				case MAIN -> getPlugin().getTopManager().getFullTopCopy();
				case MINOR -> getPlugin().getTopManager().getFullMinorTopCopy();
			};
			var playersTop = players.stream()
					.map(e -> new TopUser(
							new UserIdentifier(e.getUniqueId(), e.getName()),
							e.getBalance()))
					.toArray(TopUser[]::new);
			sender.sendPluginMessage(tag, new TopMessage(getPlugin().getProxyId(), type, playersTop).toBytePacket());
		} catch (Exception e) {	
			getPlugin().logger().severe(e, "An exception occurred while processing Top request");
		}
		return true;
	}

}
