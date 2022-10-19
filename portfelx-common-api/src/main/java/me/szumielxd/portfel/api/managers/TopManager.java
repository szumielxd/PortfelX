package me.szumielxd.portfel.api.managers;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface TopManager {
	
	
	/**
	 * Initialize TopManager
	 * 
	 * @implNote Internal use only
	 */
	public @NotNull TopManager init();
	
	/**
	 * Shutdown all operations related to userManager
	 */
	public void killManager();
	
	public boolean isInitialized();
	
	public boolean isDead();
	
	public boolean isValid();
	
	/**
	 * Get top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	public @Nullable TopEntry getByPos(int position);
	
	/**
	 * Get minor top entry at specified position. Counted from 1.
	 * 
	 * @param position position to obtain
	 * @return top entry
	 */
	public @Nullable TopEntry getByMinorPos(int position);
	
	/**
	 * Get copy of full cached top.
	 * 
	 * @return copy of actually cached top
	 */
	public @NotNull List<TopEntry> getFullTopCopy();
	
	/**
	 * Get copy of full cached minor top.
	 * 
	 * @return copy of actually cached top
	 */
	public @NotNull List<TopEntry> getFullMinorTopCopy();
	
	
	@AllArgsConstructor
	public static class TopEntry {
		
		/**
		 * player's UUID
		 */
		@Getter private final UUID uniqueId;
		/**
		 * player's name
		 */
		@Getter private final String name;
		/**
		 * player's balance
		 */
		@Getter private final long balance;
		
	}
	

}
