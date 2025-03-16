package me.szumielxd.portfel.api.objects;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public interface CommonGroupAudience<C> extends CommonAudience<C> {
	
	/**
	 * Get list of targets to sent message
	 * 
	 * @return
	 */
	public @NotNull Collection<CommonAudience<C>> getAudience();
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public default void sendMessage(@NotNull C message) {
		getAudience().forEach(a -> a.sendMessage(message));
	}
	

}
