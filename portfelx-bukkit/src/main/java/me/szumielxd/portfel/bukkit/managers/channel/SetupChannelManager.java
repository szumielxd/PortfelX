package me.szumielxd.portfel.bukkit.managers.channel;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationResultMessage.RegistrationStatus;

public class SetupChannelManager extends SpecificChannelManager {
	
	@Getter private final @NotNull String listenedChannel = Portfel.CHANNEL_SETUP;
	
	public SetupChannelManager(@NotNull PortfelBukkitImpl plugin) {
		super(plugin);
	}

	@Override
	public void onPluginMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		getPlugin().debug("PluginMessage(user|%s): %s", player.getName(), subchannel);
		switch (subchannel) {
			case REGISTER -> processRegisterMessage(player, tag, subchannel, in);
			default -> { /* nothing */}
		}
	}

	@Override
	public void clearAwaitingUpdates(@NotNull Player player) {
		// nothing to clear
	}
	
	
	private void processRegisterMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		var request = decode(RegistrationRequestMessage.class, tag, subchannel, in);
		decrypt(request.getData(), getCryptoKey(), BukkitSender.player(getPlugin(), player), subchannel, tag).ifPresent(data -> {
			var status = RegistrationStatus.INVALID;
			var serverId = getPlugin().getIdentifierManager().getComplementary(data.getProxyId());
			if (player.hasPermission("portfel.admin.register")) {
				if (serverId != null) {
					status = RegistrationStatus.SET;
				} else if (getPlugin().getIdentifierManager().register(data.getProxyId(), data.getServerId())) {
					status = RegistrationStatus.OK;
				}
			} else {
				status = RegistrationStatus.NO_PERMISSION;
			}
			sendPluginMessage(player, new RegistrationResultMessage(
					request.getOperationId(),
					new EncryptedObject<>(
							RegistrationResultMessage.CryptoPayload.class,
							new RegistrationResultMessage.CryptoPayload(
									status,
									data.getProxyId(),
									status == RegistrationStatus.SET ? serverId : data.getServerId()),
							getCryptoKey())));
		});
	}

}
