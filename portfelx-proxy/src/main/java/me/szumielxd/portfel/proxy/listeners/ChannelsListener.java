package me.szumielxd.portfel.proxy.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import lombok.AccessLevel;
import lombok.Getter;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.PluginMessageTarget;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.listeners.channel.InfoChannelListener;
import me.szumielxd.portfel.proxy.listeners.channel.SpecificChannelListener;
import me.szumielxd.portfel.proxy.listeners.channel.TransactionsChannelListener;

public abstract class ChannelsListener<T extends PortfelProxyImpl<C>, C> {
	
	@Getter(AccessLevel.PROTECTED) private final @NotNull T plugin;
	private final Map<String, SpecificChannelListener<T, C>> listenedChannels = new HashMap<>();
	
	
	protected ChannelsListener(@NotNull T plugin) {
		this.plugin = plugin;
		register(new TransactionsChannelListener<>(plugin));
		register(new InfoChannelListener<>(plugin));
	}
	
	
	protected final boolean isListendChannel(@Nullable String tag) {
		return listenedChannels.containsKey(tag);
	}
	
	
	@SuppressWarnings("unchecked")
	protected final Optional<Boolean> onPluginMessage(@NotNull PluginMessageTarget sender, @NotNull PluginMessageTarget target, @NotNull String tag, byte[] message) {
		if (sender instanceof ProxyServerConnection<?> && target instanceof ProxyPlayer<?>) {
			ProxyServerConnection<C> server = (ProxyServerConnection<C>) sender;
			ProxyPlayer<C> player = (ProxyPlayer<C>) target;
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			var channel = Objects.requireNonNull(listenedChannels.get(tag), "channel cannot be null");
			channel.onPluginMessage(server, player, tag, subchannel, in);
		}
		return Optional.of(true);
	}
	
	
	private void register(SpecificChannelListener<T, C> channel) {
		listenedChannels.put(channel.getListenedChannel(), channel);
	}
	

}
