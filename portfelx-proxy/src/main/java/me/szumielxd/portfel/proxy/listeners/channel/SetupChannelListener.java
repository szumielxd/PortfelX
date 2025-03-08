package me.szumielxd.portfel.proxy.listeners.channel;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationResultMessage.RegistrationStatus;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class SetupChannelListener<T extends PortfelProxyImpl<C>, C> extends SpecificChannelListener<T, C> {

	
	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_SETUP;
	
	
	public SetupChannelListener(@NotNull T plugin) {
		super(plugin);
	}


	@Override
	public void onPluginMessage(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		if (subchannel == SubchannelName.REGISTER) {
			onRegistrationCallback(target, tag, subchannel, in);
		}
	}
	
	
	// Setup
	// Register
	private Optional<Boolean> onRegistrationCallback(@NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		getPlugin().debug("[%s] onRegistrationCallback", "AccessManagerImpl");
		var result = decode(RegistrationResultMessage.class, tag, subchannel, in);
		getPlugin().getAccessManager().getRegistrationManager().getHolder(result.getOperationId()).ifPresent(holder -> {
			holder.done();
			decrypt(result.getData(), holder.getHashKey(), target, subchannel, tag).ifPresent(data -> {
				if (getPlugin().getProxyId().equals(data.getProxyId())) {
					if (RegistrationStatus.OK == data.getStatus() && holder.tryRegister(data.getServerId())) {
						ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_SUCCESS.draft(
								ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(
										holder.getServerFriendyName(),
										ProxyLangKey.COMMAND_VALUENAMES_SERVERFRIENDLYNAME),
								ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(
										holder.getServerId().toString(), 
										ProxyLangKey.COMMAND_VALUENAMES_SERVERID))
								.sendPrefixed(target);
					} else if (RegistrationStatus.OK == data.getStatus() && canAccess(data.getServerId())) {
						ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_ALREADY
								.draft(ProxyLangKey.MAIN_MESSAGE_INSERTION
										.draft(data.getServerId(), 
												ProxyLangKey.COMMAND_VALUENAMES_SERVERID))
								.sendPrefixed(target);
					} else {
						ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_ERROR
								.draft()
								.sendPrefixed(target);
					}
				}
				
			});
		});
		return Optional.of(true);
	}
	
	
	
	
	
	

}
