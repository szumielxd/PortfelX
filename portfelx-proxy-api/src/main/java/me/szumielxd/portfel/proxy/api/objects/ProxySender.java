package me.szumielxd.portfel.proxy.api.objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonSender;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

public interface ProxySender extends CommonSender {
	
	
	/**
	 * Send message to this sender.
	 * 
	 * @param identity identity of sender
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Identity source, @NotNull Component message);
	
	/**
	 * Send message to this sender.
	 * 
	 * @param identity identity of sender
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Identity server, @NotNull Component... message);
	
	/**
	 * Get custom display name if set, otherwise plain name.
	 * 
	 * @return sender's display name
	 */
	public @NotNull String getDisplayName();
	

}
