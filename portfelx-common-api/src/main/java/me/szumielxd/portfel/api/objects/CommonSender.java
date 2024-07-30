package me.szumielxd.portfel.api.objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;

public interface CommonSender<C> {
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public default void sendMessage(@NotNull C message) {
		sendMessage(message, false);
	}
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 * @param prefix whether send plugin prefix
	 */
	public void sendMessage(@NotNull C message, boolean prefix);
	
	/**
	 * Checks if this user has the specified permission node.
	 * 
	 * @param permission the node to check
	 * @return true if he has this node
	 */
	public boolean hasPermission(@NotNull String permission);
	
	/**
	 * Get name of this sender.
	 * 
	 * @return name of sender
	 */
	public @NotNull String getName();
	
	/**
	 * Make this sender run command.
	 * 
	 * @param command command to execute
	 */
	public void executeProxyCommand(@NotNull String command);
	
	public Portfel<C> getPlugin();
	

}
