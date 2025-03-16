package me.szumielxd.portfel.api.objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;

public interface CommonAudience<C> {
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull C message);
	
	public Portfel<C> getPlugin();
	

}
