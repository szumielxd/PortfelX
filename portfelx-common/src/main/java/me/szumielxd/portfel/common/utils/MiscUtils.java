package me.szumielxd.portfel.common.utils;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MiscUtils {
	
	/**
	 * Check if given UUID is related to premium account.
	 * 
	 * @param uuid unique ID to check
	 * @return true if this is premium UUID, otherwise false
	 */
	public static boolean isOnlineModeUUID(@Nullable UUID uuid) {
		return uuid != null && uuid.version() == 4;
	}
	

}
