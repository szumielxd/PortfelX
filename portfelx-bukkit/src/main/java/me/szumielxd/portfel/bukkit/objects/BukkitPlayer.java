package me.szumielxd.portfel.bukkit.objects;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.translation.Translator;

public class BukkitPlayer extends BukkitSender implements CommonPlayer {
	
	
	private final Player player;
	
	
	BukkitPlayer(@NotNull PortfelBukkitImpl plugin, @NotNull Player player) {
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
		this.player.kickPlayer(reason);
	}
	
	/**
	 * Connects / transfers this user to the specified connection.
	 * 
	 * @param server the new server to connect to
	 */
	public void connect(@NotNull String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(this.getName());
		out.writeUTF(server);
		this.player.sendPluginMessage(this.plugin.asPlugin(), "bungeecord:main", out.toByteArray());
	}
	
	/**
	 * Make this player run command on current server.
	 * 
	 * @param command command to execute
	 */
	public void executeServerCommand(@NotNull String command) {
		this.plugin.getServer().dispatchCommand(player, command);
	}
	
	/**
	 * Returns current player's world/server (depending on implementation).
	 * 
	 * @return name of player's current world/server
	 */
	public @NotNull String getWorldName() {
		return this.player.getWorld().getName();
	}
	
	/**
	 * Gets the player's current locale.
	 * 
	 * @return the player's locale
	 */
	public @NotNull Locale locale() {
		String locale;
		try {
			locale = (String) this.player.getClass().getMethod("getLocale").invoke(this.player);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			try {
				locale = (String) this.player.spigot().getClass().getMethod("getLocale").invoke(this.player.spigot());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				try {
					Object handle = this.player.getClass().getMethod("getHandle").invoke(this.player);
					Field f = handle.getClass().getField("locale");
					f.setAccessible(true);
					locale = (String) f.get(handle);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException exc) {
					throw new RuntimeException("Unsupported minecraft version. Missing method: getLocale", exc);
				}
			}
		}
		return Translator.parseLocale(locale);
	}
	
	/**
	 * Send this player to given world/server (depending on implementation).
	 * 
	 * @param worldName name of destination world/server
	 */
	public void sendToWorld(@NotNull String worldName) {
		World world = this.plugin.getServer().getWorld(worldName);
		if (world != null) this.player.teleport(world.getSpawnLocation());
	}
	
	/**
	 * Send ActionBar to this player.
	 * 
	 * @param message message to send
	 */
	public void sendActionBar(@NotNull Component message) {
		Audience audience = this.player instanceof Audience ? this.player : this.plugin.adventure().player(this.player);
		audience.sendActionBar(message);
	}
	
	/**
	 * Show title to this player.
	 * 
	 * @param title title to show
	 * @param subtitle subtitle to show
	 * @param times title timings (fade-in -> static -> fade-out)
	 */
	public void showTitle(@NotNull Component title, @NotNull Component subtitle, @NotNull Times times) {
		Audience audience = this.player instanceof Audience ? this.player : this.plugin.adventure().player(this.player);
		audience.showTitle(Title.title(title, subtitle, times));
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
		Audience audience = this.player instanceof Audience ? this.player : this.plugin.adventure().player(this.player);
		audience.showBossBar(bar);
		plugin.getTaskManager().runTaskLater(() -> audience.hideBossBar(bar), time.toMillis(), TimeUnit.MILLISECONDS);
	}
	

}
