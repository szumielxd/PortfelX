package me.szumielxd.portfel.bukkit.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import me.clip.placeholderapi.PlaceholderAPI;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class PlaceholderUtils {
	
	
	private static Pattern INTERNAL_PLACEHOLDERS = Pattern.compile("%(player(Id)?|balance)%");
	private static BiFunction<OfflinePlayer, String, String> REPLACE_PAPI;
	
	
	static {
		try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			REPLACE_PAPI = PlaceholderAPI::setPlaceholders;
		} catch (ClassNotFoundException e) {
			REPLACE_PAPI = (p,s) -> s;
		}
	}
	
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param player target of placeholders
	 * @param user user representation of given player
	 * @param json json object
	 */
	public static void replacePlaceholdersInJson(@NotNull User user, @NotNull JsonObject json) {
		replacePlaceholdersInJson(json, Bukkit.getOfflinePlayer(user.getUniqueId()), (match) -> {
			if (match.group().equals("%player%")) return user.getName();
			if (match.group().equals("%playerIp%")) return user.getUniqueId().toString();
			if (match.group().equals("%balance%")) return String.valueOf(user.getBalance());
			return "null";
		});
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param player target of placeholders
	 * @param user user representation of given player
	 * @param json json array
	 */
	public static void replacePlaceholdersInJson(@NotNull User user, @NotNull JsonArray json) {
		replacePlaceholdersInJson(json, Bukkit.getOfflinePlayer(user.getUniqueId()), (match) -> {
			if (match.group().equals("%player%")) return user.getName();
			if (match.group().equals("%playerIp%")) return user.getUniqueId().toString();
			if (match.group().equals("%balance%")) return String.valueOf(user.getBalance());
			return "null";
		});
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param player target of placeholders
	 * @param user user representation of given player
	 * @param text text to operate
	 */
	public static String replacePlaceholders(@NotNull User user, @NotNull String text) {
		return REPLACE_PAPI.apply(Bukkit.getOfflinePlayer(user.getUniqueId()), MiscUtils.replaceAll(INTERNAL_PLACEHOLDERS.matcher(text), match -> {
			if (match.group().equals("%player%")) return user.getName();
			if (match.group().equals("%playerIp%")) return user.getUniqueId().toString();
			if (match.group().equals("%balance%")) return String.valueOf(user.getBalance());
			return "null";
		}));
	}
	
	/**
	 * Try to parse given text as JSON component, on fail fallback to plain legacy format.
	 * 
	 * @param text text to parse
	 * @return parsed component
	 */
	public static @NotNull Component parseComponent(@NotNull String text, @NotNull User user) {
		try {
			JsonObject json = new Gson().fromJson(text, JsonObject.class);
			replacePlaceholdersInJson(user, json);
			return GsonComponentSerializer.gson().deserializeFromTree(json);
		} catch (JsonSyntaxException e) {
			return LegacyComponentSerializer.legacySection().deserialize(replacePlaceholders(user, ChatColor.translateAlternateColorCodes('&', text)));
		}
	}
	
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param json json object
	 * @param replacer replacement function
	 */
	private static void replacePlaceholdersInJson(@NotNull JsonObject json, @NotNull OfflinePlayer player, Function<MatchResult, String> replacer) {
		json.entrySet().stream().forEach(e -> {
			if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
				String val = e.getValue().getAsString();
				String newVal = REPLACE_PAPI.apply(player, MiscUtils.replaceAll(INTERNAL_PLACEHOLDERS.matcher(val), replacer));
				if (!newVal.equals(val)) json.addProperty(e.getKey(), newVal);
			} else if (e.getValue().isJsonObject()) {
				replacePlaceholdersInJson(e.getValue().getAsJsonObject(), player, replacer);
			} else if (e.getValue().isJsonArray()) {
				replacePlaceholdersInJson(e.getValue().getAsJsonArray(), player, replacer);
			}
		});
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param json json array
	 * @param pattern pattern to replace
	 * @param replacer replacement function
	 */
	public static void replacePlaceholdersInJson(@NotNull JsonArray json, @NotNull OfflinePlayer player, Function<MatchResult, String> replacer) {
		IntStream.range(0, json.size()).boxed().map(i -> new SimpleEntry<>(i, json.get(i))).forEach(e -> {
			if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
				String val = e.getValue().getAsString();
				String newVal = REPLACE_PAPI.apply(player, MiscUtils.replaceAll(INTERNAL_PLACEHOLDERS.matcher(val), replacer));
				if (!newVal.equals(val)) json.set(e.getKey(), new JsonPrimitive(newVal));
			} else if (e.getValue().isJsonObject()) {
				replacePlaceholdersInJson(e.getValue().getAsJsonObject(), player, replacer);
			} else if (e.getValue().isJsonArray()) {
				replacePlaceholdersInJson(e.getValue().getAsJsonArray(), player, replacer);
			}
		});
	}
	

}
