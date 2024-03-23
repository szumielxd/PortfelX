package me.szumielxd.portfel.proxy.api.objects;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonSender;

public interface ProxySender<C> extends CommonSender<C> {
	
	
	/**
	 * Send message to this sender.
	 * 
	 * @param identity identity of sender
	 * @param message message to send
	 */
	public void sendMessage(@Nullable UUID source, @NotNull C message);
	
	/**
	 * Get custom display name if set, otherwise plain name.
	 * 
	 * @return sender's display name
	 */
	public @NotNull String getDisplayName();
	

}
