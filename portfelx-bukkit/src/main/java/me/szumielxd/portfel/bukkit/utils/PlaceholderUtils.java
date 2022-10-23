package me.szumielxd.portfel.bukkit.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@UtilityClass
public class PlaceholderUtils {
	
	
	private static final @NotNull Pattern INTERNAL_PLACEHOLDERS = Pattern.compile("%(player(Id)?|balance)%");
	private static final @NotNull Pattern RGB_NEEDLE = Pattern.compile("&(#[a-fA-F0-9]{6})");
	private static final @NotNull BiFunction<OfflinePlayer, String, String> REPLACE_PAPI;
	private static final @NotNull BiFunction<OfflinePlayer, String, String> REPLACE_COLORED_PAPI;
	
	
	static {
		BiFunction<OfflinePlayer, String, String> replacePapi = null;
		BiFunction<OfflinePlayer, String, String> replaceColoredPapi = null;
		try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			replacePapi = PlaceholderAPI::setPlaceholders;
			replaceColoredPapi = PlaceholderUtils::setColoredPapiPlaceholders;
		} catch (ClassNotFoundException e) {
			replacePapi = (p,s) -> s;
			replaceColoredPapi = replacePapi;
		}
		REPLACE_PAPI = replacePapi;
		REPLACE_COLORED_PAPI = replaceColoredPapi;
	}
	
	
	private static @NotNull String setColoredPapiPlaceholders(@Nullable OfflinePlayer player, @NotNull String text) {
		Matcher match = PlaceholderAPI.getPlaceholderPattern().matcher(text);
		StringBuilder sb = new StringBuilder();
		int lastIndex = 0;
		while (match.find()) {
			String replacement = match.group();
			sb.append(text.substring(lastIndex, match.start()))
					.append(Optional.of(PlaceholderAPI.setPlaceholders(player, replacement))
							.filter(s -> !s.equals(replacement))
							.map(PlaceholderUtils::coloredString)
							.orElse(replacement));
			lastIndex = match.end();
		}
		sb.append(text.substring(lastIndex));
		return sb.toString();
	}
	
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param player target of placeholders
	 * @param user user representation of given player
	 * @param json json object
	 */
	public static void replacePlaceholdersInJson(@NotNull User user, @NotNull JsonObject json) {
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(json, "json cannot be null");
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
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(json, "json cannot be null");
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
	 * @param user User representation of target for placeholders
	 * @param player OfflinePlayer representation of target for placeholders
	 * @param text text to operate
	 */
	public static @NotNull String replacePlaceholders(@NotNull User user, OfflinePlayer player, @NotNull String text) {
		Objects.requireNonNull(text, "text cannot be null");
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(player, "player cannot be null");
		return REPLACE_COLORED_PAPI.apply(player, MiscUtils.replaceAll(INTERNAL_PLACEHOLDERS.matcher(text), match -> {
			if (match.group().equals("%player%")) return user.getName();
			if (match.group().equals("%playerIp%")) return user.getUniqueId().toString();
			if (match.group().equals("%balance%")) return String.valueOf(user.getBalance());
			return "null";
		}));
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param user User representation of target for placeholders
	 * @param player OfflinePlayer representation of target for placeholders
	 * @param component component to operate
	 */
	public static @NotNull Component replacePlaceholders(@NotNull User user, @NotNull OfflinePlayer player, @NotNull Component component) {
		Objects.requireNonNull(component, "component cannot be null");
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(player, "player cannot be null");
		if (component instanceof TextComponent) {
			TextComponent text = (TextComponent) component;
			TextComponent replacement = LegacyComponentSerializer.legacySection()
					.deserialize(replacePlaceholders(user, player, text.content()));
			if (replacement.children().isEmpty() && !replacement.hasStyling()) {
				component = text.content(replacement.content());
			} else {
				component = text.content("").children(Stream.concat(Stream.of(replacement), text.children().stream())
						.collect(Collectors.toList()));
			}
			
		}
		if (component.hoverEvent() != null) {
			HoverEvent<?> hover = component.hoverEvent();
			if (hover.value() instanceof Component) {
				component = component.hoverEvent(replacePlaceholders(user, player, (Component) hover.value()));
			}
		}
		component = component.children(component.children().stream().map(ch -> replacePlaceholders(user, player, ch)).collect(Collectors.toList()));
		return component;
	}
	
	/**
	 * Try to parse given text as JSON component, on fail fallback to plain legacy format.
	 * 
	 * @param text text to parse
	 * @param user User representation of target for placeholders
	 * @param player OfflinePlayer representation of target for placeholders
	 * @return parsed component
	 */
	public static @NotNull Component parseComponent(@NotNull String text, @NotNull User user, @NotNull OfflinePlayer player) {
		Objects.requireNonNull(text, "text cannot be null");
		Objects.requireNonNull(user, "user cannot be null");
		Objects.requireNonNull(player, "player cannot be null");
		try {
			JsonObject json = new Gson().fromJson(text, JsonObject.class);
			replacePlaceholdersInJson(user, json);
			return GsonComponentSerializer.gson().deserializeFromTree(json);
		} catch (JsonSyntaxException e) {
			return LegacyComponentSerializer.legacySection().deserialize(replacePlaceholders(user, player, coloredString(text)));
		}
	}
	
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param json json object
	 * @param replacer replacement function
	 */
	private static void replacePlaceholdersInJson(@NotNull JsonObject json, @NotNull OfflinePlayer player, @NotNull Function<MatchResult, String> replacer) {
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
	public static void replacePlaceholdersInJson(@NotNull JsonArray json, @NotNull OfflinePlayer player, @NotNull Function<MatchResult, String> replacer) {
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
	
	private static @NotNull String coloredString(@NotNull String text) {
		return RGB_NEEDLE.matcher(ChatColor.translateAlternateColorCodes('&', text)).replaceAll("§$1");
	}
	

}
