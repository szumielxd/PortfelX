package me.szumielxd.portfel.velocity.listeners;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.listeners.ChannelsListener;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import me.szumielxd.portfel.velocity.objects.VelocityPlayer;
import me.szumielxd.portfel.velocity.objects.VelocityServerConnection;
import net.kyori.adventure.text.Component;

public class VelocityChannelListener extends ChannelsListener<PortfelVelocityImpl, Component> {

	public VelocityChannelListener(@NotNull PortfelVelocityImpl plugin) {
		super(plugin);
	}
	
	
	@Subscribe
	public void onPluginMessageChannel(PluginMessageEvent event) {
		String tag = event.getIdentifier().getId();
		this.getPlugin().debug("[%s] Message: %s", "VelocityChannelListener", tag);
		if (this.isListendChannel(tag) && event.getSource() instanceof ServerConnection server && event.getTarget() instanceof Player player) {
			ProxyServerConnection<Component> sender = new VelocityServerConnection(this.getPlugin(), server);
			ProxyPlayer<Component> target = new VelocityPlayer(this.getPlugin(), player);
			Optional<Boolean> result = this.onPluginMessage(sender, target, tag, event.getData());
			result.ifPresent(res -> event.setResult(res.booleanValue() ? ForwardResult.handled() : ForwardResult.forward()));
		}
	}
	
	
	
	

}
