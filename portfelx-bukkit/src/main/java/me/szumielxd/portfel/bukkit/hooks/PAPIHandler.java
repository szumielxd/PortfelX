package me.szumielxd.portfel.bukkit.hooks;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import net.kyori.adventure.translation.Translator;

public class PAPIHandler extends PlaceholderExpansion {
	
	
	
	private final PortfelBukkitImpl plugin;
	private final String identifier;
	private final List<String> placeholders;
	
	
	
	public PAPIHandler(PortfelBukkitImpl plugin) {
		this.plugin = plugin;
		this.identifier = plugin.getName().toLowerCase();
		try {
			if(this.register()) this.plugin.getLogger().info("Hooked placeholders into PlaceholderAPI");
		} catch (Exception e) {
			this.plugin.getLogger().warn("Cannot hook placeholders into PlaceholderAPI");
		}
		List<String> list = new LinkedList<>();
		list.add(this.getIdentifier()+"_balance");
		list.add(this.getIdentifier()+"_balance_other_<player>");
		list.add(this.getIdentifier()+"_balance_<locale>");
		list.add(this.getIdentifier()+"_top_balance_#");
		list.add(this.getIdentifier()+"_top_balance_<locale>_#");
		list.add(this.getIdentifier()+"_top_player_#");
		list.add(this.getIdentifier()+"_top_uuid_#");
		list.add(this.getIdentifier()+"_minorbalance");
		list.add(this.getIdentifier()+"_minorbalance_other_<player>");
		list.add(this.getIdentifier()+"_minorbalance_<locale>");
		list.add(this.getIdentifier()+"_minortop_balance_#");
		list.add(this.getIdentifier()+"_minortop_balance_<locale>_#");
		list.add(this.getIdentifier()+"_minortop_player_#");
		list.add(this.getIdentifier()+"_minortop_uuid_#");
		list.replaceAll(str -> "%"+str+"%");
		this.placeholders = Collections.unmodifiableList(list);
	}
	
	
	public void oldUnregister() {
		try {
			this.getClass().getMethod("unregister").invoke(this);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			try {
				PlaceholderAPI.class.getMethod("unregisterExpansion", PlaceholderExpansion.class).invoke(null, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	
	@Override
	public String getAuthor() {
		return String.join(", ", this.plugin.asPlugin().getDescription().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public String getVersion() {
		return this.plugin.asPlugin().getDescription().getVersion();
	}
	
	@Override
	public boolean persist() {
		return true;
	}
	
	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		if(identifier.startsWith("balance")) {
			String id = identifier.substring(7);
			if(id.startsWith("_")) {
				id = id.substring(1);
				if(id.startsWith("other_")) {
					User user = this.plugin.getUserManager().getUser(id.substring(6));
					long val = user!=null? user.getBalance() : 0;
					return this.formatCurrency(player, val);
				} else {
					User user = this.plugin.getUserManager().getUser(player.getUniqueId());
					long val = user!=null? user.getBalance() : 0;
					Optional<Locale> locale = Optional.ofNullable(Translator.parseLocale(id));
					return this.formatCurrency(locale.orElse(Locale.getDefault()), val);
				}
			} else if(id.isEmpty()) {
				if(player == null) return null;
				User user = this.plugin.getUserManager().getUser(player.getUniqueId());
				long val = user!=null? user.getBalance() : 0;
				return this.formatCurrency(player, val);
			}
		}
		if(identifier.startsWith("minorbalance")) {
			String id = identifier.substring(12);
			if(id.startsWith("_")) {
				id = id.substring(1);
				if(id.startsWith("other_")) {
					User user = this.plugin.getUserManager().getUser(id.substring(6));
					long val = user!=null? user.getMinorBalance() : 0;
					return this.formatCurrency(player, val);
				} else {
					User user = this.plugin.getUserManager().getUser(player.getUniqueId());
					long val = user!=null? user.getMinorBalance() : 0;
					Optional<Locale> locale = Optional.ofNullable(Translator.parseLocale(id));
					return this.formatCurrency(locale.orElse(Locale.getDefault()), val);
				}
			} else if(id.isEmpty()) {
				if(player == null) return null;
				User user = this.plugin.getUserManager().getUser(player.getUniqueId());
				long val = user!=null? user.getMinorBalance() : 0;
				return this.formatCurrency(player, val);
			}
		}
		if(identifier.startsWith("top_balance_")) {
			String[] arr = identifier.substring(12).split("_");
			if(arr.length > 1) {
				try {
					int pos = Integer.parseInt(arr[arr.length-1]);
					BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
					TopEntry entry = user!=null ? this.plugin.getTopManager().getByPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByPos(pos);
					Optional<Locale> locale = Optional.ofNullable(Translator.parseLocale(String.join("_", Arrays.copyOf(arr, arr.length-1))));
					return entry!=null? this.formatCurrency(locale.orElse(Locale.getDefault()), entry.getBalance()) : "";
				} catch (NumberFormatException e) {}
			} else {
				try {
					int pos = Integer.parseInt(identifier.substring(12));
					BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
					TopEntry entry = user!=null ? this.plugin.getTopManager().getByPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByPos(pos);
					return entry!=null? this.formatCurrency(player, entry.getBalance()) : "";
				} catch (NumberFormatException e) {}
			}
			return null;
		}
		if(identifier.startsWith("top_player_")) {
			try {
				int pos = Integer.parseInt(identifier.substring(11));
				BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
				TopEntry entry = user!=null ? this.plugin.getTopManager().getByPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByPos(pos);
				return entry!=null? entry.getName() : "";
			} catch (NumberFormatException e) {}
			return null;
		}
		if(identifier.startsWith("top_uuid_")) {
			try {
				int pos = Integer.parseInt(identifier.substring(9));
				BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
				TopEntry entry = user!=null ? this.plugin.getTopManager().getByPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByPos(pos);
				return entry!=null? entry.getUniqueId().toString() : "";
			} catch (NumberFormatException e) {}
			return null;
		}
		if(identifier.startsWith("minortop_balance_")) {
			String[] arr = identifier.substring(17).split("_");
			if(arr.length > 1) {
				try {
					int pos = Integer.parseInt(arr[arr.length-1]);
					BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
					TopEntry entry = user!=null ? this.plugin.getTopManager().getByMinorPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByMinorPos(pos);
					Optional<Locale> locale = Optional.ofNullable(Translator.parseLocale(String.join("_", Arrays.copyOf(arr, arr.length-1))));
					return entry!=null? this.formatCurrency(locale.orElse(Locale.getDefault()), entry.getBalance()) : "";
				} catch (NumberFormatException e) {}
			} else {
				try {
					int pos = Integer.parseInt(identifier.substring(12));
					BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
					TopEntry entry = user!=null ? this.plugin.getTopManager().getByMinorPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByMinorPos(pos);
					return entry!=null? this.formatCurrency(player, entry.getBalance()) : "";
				} catch (NumberFormatException e) {}
			}
			return null;
		}
		if(identifier.startsWith("minortop_player_")) {
			try {
				int pos = Integer.parseInt(identifier.substring(16));
				BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
				TopEntry entry = user!=null ? this.plugin.getTopManager().getByMinorPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByMinorPos(pos);
				return entry!=null? entry.getName() : "";
			} catch (NumberFormatException e) {}
			return null;
		}
		if(identifier.startsWith("minortop_uuid_")) {
			try {
				int pos = Integer.parseInt(identifier.substring(14));
				BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
				TopEntry entry = user!=null ? this.plugin.getTopManager().getByMinorPos(user.getRemoteId(), pos) : this.plugin.getTopManager().getByMinorPos(pos);
				return entry!=null? entry.getUniqueId().toString() : "";
			} catch (NumberFormatException e) {}
			return null;
		}
		return null;
	}
	
	@Override
	public List<String> getPlaceholders() {
		return this.placeholders;
	}
	
	
	private String formatCurrency(OfflinePlayer player, long value) {
		Locale locale = player.getPlayer() == null ? Locale.getDefault() : ((CommonPlayer)BukkitSender.wrap(this.plugin, player.getPlayer())).locale();
		return this.formatCurrency(locale, value);
	}
	
	
	private String formatCurrency(Locale locale, long value) {
		NumberFormat format = NumberFormat.getInstance(locale);
		format.setMinimumFractionDigits(2);
		format.setMaximumFractionDigits(2);
		return Lang.get(locale).text(LangKey.MAIN_CURRENCY_FORMAT, format.format(value).replace(' ', ' '));
	}
	

}
