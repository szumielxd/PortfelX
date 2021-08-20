package me.szumielxd.portfel.api.objects;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title.Times;

public interface CommonPlayer extends CommonSender {

	/**
	 * Make this player chat (say something).
	 * 
	 * @param message message to print
	 */
	public void chat(@NotNull String message);
	
	/**
	 * Returns the UUID of this player.
	 * 
	 * @return Player UUID
	 */
	public @NotNull UUID getUniqueId();
	
	/**
	 * Kicks player with custom kick message.
	 * 
	 * @param reason kick message
	 */
	public void disconnect(@NotNull String reason);
	
	/**
	 * Connects / transfers this user to the specified connection.
	 * 
	 * @param server the new server to connect to
	 */
	public void connect(@NotNull String server);
	
	/**
	 * Make this player run command on current server.
	 * 
	 * @param command command to execute
	 */
	public void executeServerCommand(@NotNull String command);
	
	/**
	 * Returns current player's world/server (depending on implementation).
	 * 
	 * @return name of player's current world/server
	 */
	public @NotNull String getWorldName();
	
	/**
	 * Gets the player's current locale.
	 * 
	 * @return the player's locale
	 */
	public @NotNull Locale locale();
	
	/**
	 * Send this player to given world/server (depending on implementation).
	 * 
	 * @param worldName name of destination world/server
	 */
	public void sendToWorld(@NotNull String worldName);
	
	/**
	 * Send ActionBar to this player.
	 * 
	 * @param message message to send
	 */
	public void sendActionBar(@NotNull Component message);
	
	/**
	 * Show title to this player.
	 * 
	 * @param title title to show
	 * @param subtitle subtitle to show
	 * @param times title timings (fade-in -> static -> fade-out)
	 */
	public void showTitle(@NotNull Component title, @NotNull Component subtitle, @NotNull Times times);
	
	/**
	 * Show BosBar to this player.
	 * 
	 * @param name name of bossbar
	 * @param time time duration to show bossbar
	 * @param progress progress of bossbar
	 * @param color color of bossbar
	 * @param overlay style of bossbar
	 * @param flags additional flags of bossbar
	 */
	public void showBossBar(@NotNull Component name, @NotNull Duration time, float progress, @NotNull Color color, @NotNull Overlay overlay, @NotNull Flag... flags);

}
