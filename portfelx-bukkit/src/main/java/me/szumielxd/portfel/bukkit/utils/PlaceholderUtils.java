package me.szumielxd.portfel.bukkit.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

@UtilityClass
public class PlaceholderUtils {
	
	
	private static final @NotNull Pattern RGB_NEEDLE = Pattern.compile("&(#[a-fA-F0-9]{6})");
	
	
	public Map<String, MessageDraft> userPlaceholders(@NotNull User user) {
		return userPlainPlaceholders(user).entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> MessageDraft.plain(e.getValue())));
	}
	
	public Map<String, String> userPlainPlaceholders(@NotNull User user) {
		return Map.of(
				"player", user.getName(),
				"placerId", user.getUniqueId().toString(),
				"balance", String.valueOf(user.getBalance()),
				"minorBalance", String.valueOf(user.getMinorBalance()));
	}
	
	
	public @NotNull String getPlain(@NotNull String text, @NotNull Player player, @NotNull User user, @NotNull Map<String, String> extra) {
		var replacements = new HashMap<>(userPlainPlaceholders(user));
		replacements.putAll(extra);
		return PlaceholderAPI.getPlaceholderPattern().matcher(text).replaceAll(
				m -> Optional.ofNullable(replacements.get(m.group(1)))
						.orElseGet(() -> PlaceholderAPI.setPlaceholders(player, m.group())));
	}
	
	
	public @NotNull String getPlain(@NotNull String text, @NotNull Player player, @NotNull User user) {
		return getPlain(text, player, user, Map.of());
	}
	
	
	public @NotNull MessageDraft getDraft(@NotNull String text, @NotNull Player player, @NotNull User user, @NotNull Map<String, MessageDraft> extra) {
		var replacements = new HashMap<>(userPlaceholders(user));
		replacements.putAll(extra);
		var draft = parseInternal(MessageDraft.minimessage(text), replacements);
		draft = parsePAPI(draft, player);
		return draft;
	}
	
	
	public @NotNull MessageDraft getDraft(@NotNull String text, @NotNull Player player, @NotNull User user) {
		return getDraft(text, player, user, Map.of());
	}
	
	
	private @NotNull MessageDraft parseInternal(@NotNull MessageDraft draft, @NotNull Map<String, MessageDraft> replacements) {
		return draft.deepPlaceholders(
				PlaceholderAPI.getPlaceholderPattern(),
				s -> replacements.get(s.substring(1, s.length() - 1)));
	}
	
	
	private @NotNull MessageDraft parsePAPI(@NotNull MessageDraft draft, @NotNull Player player) {
		return draft.deepPlaceholders(
				PlaceholderAPI.getPlaceholderPattern(),
				s -> Optional.ofNullable(getPAPIPlaceholderReplacement(s, player))
						.map(MessageDraft::minimessage)
						.orElse(null));
	}
	
	
	private @Nullable String getPAPIPlaceholderReplacement(@NotNull String text, @NotNull Player player) {
		var mgr = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
		int index = text.indexOf('_');
		if (index > -1) {
			var expansion = mgr.getExpansion(text.substring(1, index));
			if (expansion != null) {
				var result = expansion.onRequest(player, text.substring(index, text.length() - 1));
				if (result != null) {
					return PlaceholderUtils.coloredString(result);
				}
			}
		}
		return null;
	}
	
	
	private @NotNull Function<MatchResult, String> internalPlaceholdersReplacer(@NotNull Lang lang, @NotNull User user) {
		var placeholders = userPlaceholders(user);
		return match -> Optional.ofNullable(placeholders.get(match.group(1)))
					.map(d -> d.buildPlain(lang))
					.orElse("null");
	}
	
	private static @NotNull String coloredString(@NotNull String text) {
		return RGB_NEEDLE.matcher(text).replaceAll("§$1");
	}
	

}
