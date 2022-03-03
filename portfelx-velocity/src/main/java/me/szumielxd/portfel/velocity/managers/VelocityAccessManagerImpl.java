package me.szumielxd.portfel.velocity.managers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import me.szumielxd.portfel.velocity.objects.VelocityPlayer;
import me.szumielxd.portfel.velocity.objects.VelocityServerConnection;

public class VelocityAccessManagerImpl extends AccessManagerImpl {

	public VelocityAccessManagerImpl(@NotNull PortfelProxyImpl plugin) {
		super(plugin);
	}

	@Override
	protected void preInit() {
	}

	@Override
	protected void postInit() {
		PortfelVelocityImpl portfel = (PortfelVelocityImpl) this.getPlugin();
		portfel.getProxy().getEventManager().register(portfel.asPlugin(), this);
	}
	
	
	@Subscribe
	public void onPluginMessageChannel(PluginMessageEvent event) {
		String tag = event.getIdentifier().toString();
		if (this.isListendChannel(tag)) {
			if (event.getSource() instanceof ServerConnection && event.getTarget() instanceof Player) {
				ServerConnection server = (ServerConnection) event.getSource();
				Player player = (Player) event.getTarget();
				ProxyServerConnection sender = new VelocityServerConnection((PortfelVelocityImpl) this.getPlugin(), server);
				ProxyPlayer target = new VelocityPlayer((PortfelVelocityImpl) this.getPlugin(), player);
				Optional<Boolean> result = this.onPluginMessage(sender, target, tag, event.getData());
				if (result.isPresent()) {
					event.setResult(result.get() ? ForwardResult.handled() : ForwardResult.forward());
				}
			}
		}
	}
	

}
