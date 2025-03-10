package me.szumielxd.portfel.proxy.listeners;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.common.utils.future.CompletableUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;

@RequiredArgsConstructor
public abstract class UserListener<T extends PortfelProxyImpl<C>, C> {
	
	
	@Getter private final @NotNull T plugin;

	
	protected void onConnect(@NotNull ProxyPlayer<C> player, @NotNull ProxyServerConnection<C> server) {
		this.plugin.debug("UserListener::onConnect(%s, %s)", player, server);
		plugin.getUserManager().getOrCreateUser(player.getUniqueId(), player.getName(), true)
				.whenComplete(CompletableUtils.handleResult(user -> {
					user.setRemoteId(null);
					player.getServer()
							.filter(server::equals)
							.ifPresent(user::sendInfoPacket);
				}, e -> getPlugin().logger().severe(e, "An exception occurred while sending user info")));
	}
	

}
