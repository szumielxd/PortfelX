package me.szumielxd.portfel.bungee.objects;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeSender implements ProxySender<BaseComponent[]> {
	
	
	protected final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull CommandSender sender;
	
	
	public BungeeSender(@NotNull PortfelBungeeImpl plugin, @NotNull CommandSender sender) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.sender = Objects.requireNonNull(sender, "sender cannot be null");
	}

	@Override
	public void sendMessage(@NotNull BaseComponent[] message) {
		this.sender.sendMessage(message);
	}
	
	@Override
	public void sendMessage(@Nullable UUID source, @NotNull BaseComponent[] message) {
		this.sender.sendMessage(message);
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	@Override
	public void sendTranslated(@NotNull BaseComponent[] message) {
		//this.plugin.adventure().sender(this.sender).sendMessage(Lang.get(this).translateComponent(message));
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
		this.plugin.asPlugin().getProxy().getPluginManager().dispatchCommand(this.sender, command);
	}
	
	
	public static @NotNull BungeeSender wrap(@NotNull PortfelBungeeImpl plugin, @NotNull CommandSender sender) {
		if (Objects.requireNonNull(sender, "sender cannot be null") instanceof ProxiedPlayer) return new BungeePlayer(plugin, (ProxiedPlayer) sender);
		return new BungeeSender(plugin, sender);
	}
	

}
