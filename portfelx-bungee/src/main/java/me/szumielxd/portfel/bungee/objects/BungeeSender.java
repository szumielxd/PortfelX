package me.szumielxd.portfel.bungee.objects;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class BungeeSender implements ProxySender<BaseComponent[]> {
	
	
	@Getter protected final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull CommandSender sender;

	@Override
	public void sendMessage(@NotNull BaseComponent[] message) {
		this.sender.sendMessage(message);
	}
	
	@Override
	public void sendMessage(@Nullable UUID source, @NotNull BaseComponent[] message) {
		this.sender.sendMessage(message);
	}
	
	@Override
	public boolean hasPermission(@NotNull String permission) {
		return this.sender.hasPermission(permission);
	}

	@Override
	public @NotNull String getName() {
		return "Console";
	}
	
	@Override
	public @NotNull String getDisplayName() {
		return this.getName();
	}

	@Override
	public void executeProxyCommand(@NotNull String command) {
		this.plugin.getProxy().getPluginManager().dispatchCommand(this.sender, command);
	}
	
	
	public static @NotNull BungeeSender wrap(@NotNull PortfelBungeeImpl plugin, @NotNull CommandSender sender) {
		if (Objects.requireNonNull(sender, "sender cannot be null") instanceof ProxiedPlayer) return new BungeePlayer(plugin, (ProxiedPlayer) sender);
		return new BungeeSender(plugin, sender);
	}
	

}
