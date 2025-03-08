package me.szumielxd.portfel.proxy.listeners.channel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;
import me.szumielxd.portfel.common.communication.coders.PacketCoder;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.utils.CryptoUtils.DecryptionException;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;

@RequiredArgsConstructor
public abstract class SpecificChannelListener<T extends PortfelProxyImpl<C>, C> {
	
	@Getter(AccessLevel.PROTECTED) private final @NotNull T plugin;
	
	public abstract @NotNull String getListenedChannel();
	
	public abstract void onPluginMessage(@NotNull ProxyServerConnection<C> sender, @NotNull ProxyPlayer<C> target, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in);
	
	public boolean isListening(@NotNull String channel) {
		return getListenedChannel().equals(channel);
	}
	
	protected boolean canAccess(@NotNull UUID serverId) {
		return plugin.getAccessManager().canAccess(serverId);
	}
	
	
	protected boolean canAccess(@NotNull UUID serverId, @NotNull String permission) {
		return plugin.getAccessManager().canAccess(serverId, permission);
	}
	
	
	protected @NotNull String getCryptoKey(@NotNull UUID serverId) {
		return Objects.requireNonNull(this.plugin.getAccessManager().getHashKey(serverId), "Invalid server");
	}
	
	protected <E extends MessagePacket> E decode(Class<E> clazz, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		return PacketCoder.decode(in, clazz)
				.orElseThrow(() -> new IllegalArgumentException(
						"Unknown packet structure for subchannel `%s` in channel `%s`".formatted(subchannel.getName(), tag)));
	}
	
	protected @NotNull <E> Optional<E> decrypt(@NotNull EncryptedObject<E> obj, @NotNull String cryptoKey, @NotNull ProxyPlayer<C> player, @NotNull SubchannelName subchannel, @NotNull String tag) {
		try {
			return Optional.of(obj.decrypt(cryptoKey));
		} catch (DecryptionException e) {
			plugin.logger().warn(e, "Couldn't decrypt data for player `%s` in subchannel `%s` in channel `%s`"
					.formatted(player.getName(), subchannel.getName(), tag));
			return Optional.empty();
		}
	}
	
	protected @NotNull <E> Optional<E> decrypt(@NotNull EncryptedObject<E> obj, @NotNull UUID serverId, @NotNull ProxyPlayer<C> player, @NotNull SubchannelName subchannel, @NotNull String tag) {
		return decrypt(obj, getCryptoKey(serverId), player, subchannel, tag);
	}

}
