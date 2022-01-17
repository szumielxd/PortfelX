package me.szumielxd.portfel.velocity.objects;

import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocitySender implements ProxySender {
	
	
	protected final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull CommandSource sender;
	
	
	public VelocitySender(@NotNull PortfelVelocityImpl plugin, @NotNull CommandSource sender) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.sender = Objects.requireNonNull(sender, "sender cannot be null");
	}
	

	@Override
	public void sendMessage(@NotNull String message) {
		this.sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		this.sender.sendMessage(message);
		
	}

	@Override
	public void sendMessage(@NotNull Component... message) {
		this.sender.sendMessage(Component.empty().children(Arrays.asList(message)));
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component message) {
		this.sender.sendMessage(source, message);
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component... message) {
		this.sender.sendMessage(source, Component.empty().children(Arrays.asList(message)));
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component message) {
		Component comp = Lang.get(this).translateComponent(message);
		this.sender.sendMessage(comp);
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component... message) {
		this.sender.sendMessage(Lang.get(this).translateComponent(Component.empty().children(Arrays.asList(message))));
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
		this.plugin.getProxy().getCommandManager().executeAsync(this.sender, command);
	}
	
	
	public static @NotNull VelocitySender wrap(@NotNull PortfelVelocityImpl plugin, @NotNull CommandSource sender) {
		if (Objects.requireNonNull(sender, "sender cannot be null") instanceof Player) return new VelocityPlayer(plugin, (Player) sender);
		return new VelocitySender(plugin, sender);
	}
	
	@Override
	public int hashCode() {
		return this.sender.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.sender.equals(obj);
	}
	

}
