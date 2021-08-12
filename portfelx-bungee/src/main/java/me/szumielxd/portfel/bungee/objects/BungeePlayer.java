package me.szumielxd.portfel.bungee.objects;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.objects.CommonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeePlayer extends BungeeSender implements CommonPlayer {
	
	
	private final ProxiedPlayer player;
	
	
	public BungeePlayer(@NotNull PortfelBungee plugin, @NotNull ProxiedPlayer player) {
		super(plugin, player);
		this.player = player;
	}
	
	
	/**
	 * Make this player chat (say something).
	 * 
	 * @param message message to print
	 */
	public void chat(@NotNull String message) {
		this.player.chat(message);
	}
	
	/**
	 * Returns the UUID of this player.
	 * 
	 * @return Player UUID
	 */
	public @NotNull UUID getUniqueId() {
		return this.player.getUniqueId();
	}
	
	/**
	 * Kicks player with custom kick message.
	 * 
	 * @param reason kick message
	 */
	public void disconnect(@NotNull String reason) {
		this.player.disconnect(TextComponent.fromLegacyText(reason));
	}
	
	/**
	 * Connects / transfers this user to the specified connection.
	 * 
	 * @param server the new server to connect to
	 */
	public void connect(@NotNull String server) {
		ServerInfo info = this.plugin.getProxy().getServerInfo(server);
		if (info != null) this.player.connect(info);
	}
	
	/**
	 * Make this player run command on current server.
	 * 
	 * @param command command to execute
	 */
	public void executeServerCommand(@NotNull String command) {
		if (command.startsWith("/")) this.player.chat(command);
		else this.player.chat('/'+command);
	}
	
	/**
	 * Returns current player's world/server (depending on implementation).
	 * 
	 * @return name of player's current world/server
	 */
	public @NotNull String getWorldName() {
		return this.player.getServer().getInfo().getName();
	}
	
	/**
	 * Gets the player's current locale.
	 * 
	 * @return the player's locale
	 */
	public @NotNull Locale locale() {
		return this.player.getLocale();
	}
	
	/**
	 * Send this player to given world/server (depending on implementation).
	 * 
	 * @param worldName name of destination world/server
	 */
	public void sendToWorld(@NotNull String worldName) {
		ServerInfo info = this.plugin.getProxy().getServerInfo(worldName);
		if (info != null) this.player.connect(info);
	}
	
	/**
	 * Send ActionBar to this player.
	 * 
	 * @param message message to send
	 */
	public void sendActionBar(@NotNull Component message) {
		this.plugin.adventure().player(this.player).sendActionBar(message);
	}
	
	/**
	 * Show title to this player.
	 * 
	 * @param title title to show
	 * @param subtitle subtitle to show
	 * @param times title timings (fade-in -> static -> fade-out)
	 */
	public void showTitle(@NotNull Component title, @NotNull Component subtitle, @NotNull Times times) {
		this.plugin.adventure().player(this.player).showTitle(Title.title(title, subtitle, times));
	}
	
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
	public void showBossBar(@NotNull Component name, @NotNull Duration time, float progress, @NotNull Color color, @NotNull Overlay overlay, @NotNull Flag... flags) {
		final BossBar bar = BossBar.bossBar(name, progress, color, overlay, new HashSet<>(Arrays.asList(flags)));
		final Audience a = this.plugin.adventure().player(player);
		a.showBossBar(bar);
		plugin.getTaskManager().runTaskLater(() -> a.hideBossBar(bar), time.toMillis(), TimeUnit.MILLISECONDS);
	}
	

}
