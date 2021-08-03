package me.szumielxd.portfel.common.objects;

import java.util.UUID;

public abstract class ActionExecutor {
	
	
	public static UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static UUID PLUGIN_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	
	
	private final String displayName;
	private final UUID uuid;
	
	
	protected ActionExecutor(String displayName, UUID uuid) {
		this.displayName = displayName;
		this.uuid = uuid;
	}
	
	
	/**
	 * Get unique identifier of ActionExecutor.
	 * 
	 * @return UUID of executor
	 */
	public UUID getUniqueId() {
		return this.uuid;
	}
	
	/**
	 * Get display name of ActionExecutor.
	 * 
	 * @return display name of executor
	 */
	public String getDisplayName() {
		return this.displayName;
	}
	

}
