package me.szumielxd.portfel.bungee.objects;

import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeSender implements ProxySender {
	
	
	protected final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull CommandSender sender;
	
	
	public BungeeSender(@NotNull PortfelBungeeImpl plugin, @NotNull CommandSender sender) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.sender = Objects.requireNonNull(sender, "sender cannot be null");
	}
	

	@Override
	public void sendMessage(@NotNull String message) {
		this.plugin.adventure().sender(this.sender).sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		this.plugin.adventure().sender(this.sender).sendMessage(message);
		
	}

	@Override
	public void sendMessage(@NotNull Component... message) {
		this.plugin.adventure().sender(this.sender).sendMessage(Component.empty().children(Arrays.asList(message)));
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component message) {
		this.plugin.adventure().sender(this.sender).sendMessage(source, message);
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component... message) {
		this.plugin.adventure().sender(this.sender).sendMessage(source, Component.empty().children(Arrays.asList(message)));
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component message) {
		this.plugin.adventure().sender(this.sender).sendMessage(Lang.get(this).translateComponent(message));
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component... message) {
		this.plugin.adventure().sender(this.sender).sendMessage(Lang.get(this).translateComponent(Component.empty().children(Arrays.asList(message))));
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
