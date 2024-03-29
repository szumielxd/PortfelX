package me.szumielxd.portfel.bukkit.objects;

import java.util.Arrays;
import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.common.Lang;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;


public class BukkitSender implements CommonSender {
	
	
	protected final PortfelBukkitImpl plugin;
	private final CommandSender sender;
	
	
	BukkitSender(@NotNull PortfelBukkitImpl plugin, @NotNull CommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
	}
	
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull String message) {
		this.sender.sendMessage(message);
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	@SuppressWarnings("deprecation")
	public void sendMessage(@NotNull BaseComponent message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		this.sender.sendMessage(BaseComponent.toLegacyText(message));
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	@SuppressWarnings("deprecation")
	public void sendMessage(@NotNull BaseComponent... message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		this.sender.sendMessage(BaseComponent.toLegacyText(message));
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component message) {
		Audience audience = this.sender instanceof Audience ? this.sender : this.plugin.adventure().sender(this.sender);
		audience.sendMessage(message);
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component... message) {
		Audience audience = this.sender instanceof Audience ? this.sender : this.plugin.adventure().sender(this.sender);
		audience.sendMessage(Component.empty().children(Arrays.asList(message)));
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component message) {
		Audience audience = this.sender instanceof Audience ? this.sender : this.plugin.adventure().sender(this.sender);
		audience.sendMessage(Lang.get(this).translateComponent(message));
	}
	
	/**
	 * Translate and send message to this sender.
	 * 
	 * @param message message to translate and send
	 */
	public void sendTranslated(@NotNull Component... message) {
		Audience audience = this.sender instanceof Audience ? this.sender : this.plugin.adventure().sender(this.sender);
		audience.sendMessage(Lang.get(this).translateComponent(Component.empty().children(Arrays.asList(message))));
	}
	
	/**
	 * Checks if this user has the specified permission node.
	 * 
	 * @param permission the node to check
	 * @return true if he has this node
	 */
	public boolean hasPermission(@NotNull String permission) {
		return this.sender.hasPermission(permission);
	}
	
	/**
	 * Get name of this sender.
	 * 
	 * @return name of sender
	 */
	public @NotNull String getName() {
		return this.sender.getName();
	}
	
	/**
	 * Make this sender run command.
	 * 
	 * @param command command to execute
	 */
	public void executeProxyCommand(@NotNull String command) {
		this.plugin.getServer().dispatchCommand(this.sender, command);
	}
	
	
	/**
	 * Creates CommonSender or CommonPlayer depending on given CommandSender.
	 * 
	 * @param plugin Portfel instance
	 * @param sender sender to wrap
	 * @return CommonSender or its subclass
	 */
	public static @NotNull BukkitSender wrap(@NotNull PortfelBukkitImpl plugin, @NotNull CommandSender sender) {
		if (Objects.requireNonNull(sender, "sender cannot be null") instanceof Player) return new BukkitPlayer(plugin, (Player) sender);
		return new BukkitSender(plugin, sender);
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
