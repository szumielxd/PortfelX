package me.szumielxd.portfel.bukkit.managers.channel;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitImaginaryUser;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;
import me.szumielxd.portfel.common.communication.coders.PacketCoder;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.utils.CryptoUtils.DecryptionException;

@RequiredArgsConstructor
public abstract class SpecificChannelManager implements PluginMessageListener {
	
	@Getter(AccessLevel.PROTECTED) private final @NotNull PortfelBukkitImpl plugin;
	
	public abstract @NotNull String getListenedChannel();
	
	public abstract void onPluginMessage(@NotNull Player player, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in);
	
	public abstract void clearAwaitingUpdates(@NotNull Player player);
	
	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
		if (channel.equals(getListenedChannel())) {
			var in = ByteStreams.newDataInput(message);
			SubchannelName.getByName(in.readUTF())
					.ifPresent(subchannel -> onPluginMessage(player, channel, subchannel, in));
		}
	}
	
	public boolean isListening(@NotNull String channel) {
		return getListenedChannel().equals(channel);
	}
	
	
	protected @NotNull String getCryptoKey() {
		return getPlugin().getServerHashKey();
	}
	
	protected <E extends MessagePacket> E decode(Class<E> clazz, @NotNull String tag, @NotNull SubchannelName subchannel, @NotNull ByteArrayDataInput in) {
		return PacketCoder.decode(in, clazz)
				.orElseThrow(() -> new IllegalArgumentException(
						"Unknown packet structure for subchannel `%s` in channel `%s`".formatted(subchannel.getName(), tag)));
	}
	
	protected @NotNull <E> Optional<E> decrypt(@NotNull EncryptedObject<E> obj, @NotNull String cryptoKey, @NotNull BukkitPlayer player, @NotNull SubchannelName subchannel, @NotNull String tag) {
		try {
			return Optional.of(obj.decrypt(cryptoKey));
		} catch (DecryptionException e) {
			getPlugin().logger().warn(e, "Couldn't decrypt data for player `%s` in subchannel `%s` in channel `%s`"
					.formatted(player.getName(), subchannel.getName(), tag));
			return Optional.empty();
		}
	}
	
	protected @NotNull <E> Optional<E> decrypt(@NotNull EncryptedObject<E> obj, @NotNull UUID serverId, @NotNull BukkitPlayer player, @NotNull SubchannelName subchannel, @NotNull String tag) {
		return decrypt(obj, getCryptoKey(), player, subchannel, tag);
	}
	
	protected @Nullable UUID getServerId(@NotNull UUID proxyId) {
		return getPlugin().getIdentifierManager().getComplementary(proxyId);
	}
	
	protected boolean isValidProxy(@NotNull UUID proxyId) {
		return getPlugin().getIdentifierManager().isValid(proxyId);
	}
	
	protected boolean isValidServer(@NotNull UUID proxyId, @NotNull UUID serverId) {
		return serverId.equals(getServerId(proxyId));
	}
	
	protected void sendPluginMessage(@NotNull Player player, @NotNull MessagePacket message) {
		player.sendPluginMessage(getPlugin(), getListenedChannel(), message.toBytePacket());
	}
	
	protected boolean isValidUser(@Nullable User user) {
		return user != null && !(user instanceof BukkitImaginaryUser);
	}

}
