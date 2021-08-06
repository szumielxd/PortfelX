package me.szumielxd.portfel.bukkit.objects;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.objects.CommonSender;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;


public class BukkitSender implements CommonSender {
	
	
	protected final PortfelBukkit plugin;
	private final CommandSender sender;
	
	
	public BukkitSender(@NotNull PortfelBukkit plugin, @NotNull CommandSender sender) {
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
	public void sendMessage(@NotNull BaseComponent message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		this.sender.sendMessage(TextComponent.toLegacyText(message));
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull BaseComponent... message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		this.sender.sendMessage(TextComponent.toLegacyText(message));
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component message) {
		this.plugin.adventure().sender(this.sender).sendMessage(message);
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component... message) {
		this.plugin.adventure().sender(this.sender).sendMessage(Component.empty().children(Arrays.asList(message)));
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
	

}
