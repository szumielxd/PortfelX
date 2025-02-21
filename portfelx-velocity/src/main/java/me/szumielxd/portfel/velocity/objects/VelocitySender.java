package me.szumielxd.portfel.velocity.objects;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class VelocitySender implements ProxySender<Component> {
	
	
	@Getter protected final @NotNull PortfelVelocityImpl plugin;
	private final @NotNull CommandSource sender;

	@Override
	public void sendMessage(@NotNull Component message) {
		this.sender.sendMessage(message);	
	}

	@SuppressWarnings("deprecation")
	@Override
	public void sendMessage(@Nullable UUID source, @NotNull Component message) {
		this.sender.sendMessage(Identity.identity(source), message);
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
