package me.szumielxd.portfel.bukkit.hooks;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import net.kyori.adventure.translation.Translator;

public class MVdWHandler {
	
	
	
	private final PortfelBukkitImpl plugin;
	public final PlaceholderReplacer simpleBalance;
	public final PlaceholderReplacer extendedBalance;
	public final PlaceholderReplacer topBalance;
	public final PlaceholderReplacer topPlayer;
	public final PlaceholderReplacer topUUID;
	
	
	
	public MVdWHandler(PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		
		// simpleBalance
		this.simpleBalance = event -> {
			if(event.getPlayer() != null && event.getPlayer().isOnline()) {
				User user = this.plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
				long value = user != null? user.getBalance() : 0;
				return this.formatCurrency(event.getPlayer(), value);
			}
			return null;
		};
		// extendedBalance
		this.extendedBalance = event -> {
			String str = event.getPlaceholder().substring(16);
			if(str.startsWith("other_")) {
				Player p = Bukkit.getPlayerExact(str.substring(6));
				User user = p != null ? this.plugin.getUserManager().getUser(p.getUniqueId()) : null;
				long value = user != null ? user.getBalance() : 0;
				return this.formatCurrency(event.getPlayer(), value);
			} else {
				User user = MVdWHandler.this.plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
				long value = user != null ? user.getBalance() : 0;
				Optional<Locale> locale = Optional.ofNullable(Translator.parseLocale(str));
				return this.formatCurrency(locale.orElse(Locale.getDefault()), value);
			}
		};
		// topBalance
		this.topBalance = event -> {
			String[] arr = event.getPlaceholder().substring(20).split("_", 2);
			try {
				int pos = Integer.parseInt(arr[arr.length-1]);
				TopEntry top = this.plugin.getTopManager().getByPos(pos);
				if (top == null) return "";
				return this.formatCurrency(event.getPlayer(), top.getBalance());
			} catch (NumberFormatException e) {}
			return "";
		};
		// topPlayer
		this.topPlayer = event -> {
			try {
				int pos = Integer.parseInt(event.getPlaceholder().substring(19));
				TopEntry top = this.plugin.getTopManager().getByPos(pos);
				return top != null ? top.getName() : "";
			} catch (NumberFormatException e) {}
			return "";
		};
		// topUUID
		this.topUUID = event -> {
			try {
				int pos = Integer.parseInt(event.getPlaceholder().substring(19));
				TopEntry top = this.plugin.getTopManager().getByPos(pos);
				return top != null ? String.valueOf(top.getUniqueId()) : "";
			} catch (NumberFormatException e) {}
			return "";
		};
		
		// REGISTER
		if(PlaceholderAPI.registerPlaceholder(this.plugin, "portfel_balance", this.simpleBalance)
				&& PlaceholderAPI.registerPlaceholder(this.plugin, "portfel_balance_*", this.extendedBalance)
				&& PlaceholderAPI.registerPlaceholder(this.plugin, "portfel_top_balance_*", this.topBalance)
				&& PlaceholderAPI.registerPlaceholder(this.plugin, "portfel_top_uuid_*", this.topUUID)
				&& PlaceholderAPI.registerPlaceholder(this.plugin, "portfel_top_player_*", this.topPlayer)) {
			this.plugin.getLogger().info("Hooked placeholders into MVdWPlaceholderAPI");
		}
	}
	
	
	private String formatCurrency(OfflinePlayer player, long value) {
		Locale locale = player.getPlayer() == null ? Locale.getDefault() : ((CommonPlayer)BukkitSender.get(this.plugin, player.getPlayer())).locale();
		return this.formatCurrency(locale, value);
	}
	
	
	private String formatCurrency(Locale locale, long value) {
		return Lang.get(locale).text(LangKey.MAIN_CURRENCY_FORMAT, NumberFormat.getInstance(locale).format(value).replace(' ', ' '));
	}
	
	
	
	public void unregister() {
	}
	

}
