package me.szumielxd.portfel.common.lang.draft;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class PlaceholdersMessageDraft extends MessageDraft {

	private static final @NotNull Pattern PLACEHOLDER_PATTERN = Pattern.compile("[a-z\\d]+", Pattern.CASE_INSENSITIVE);
	
	private final @NotNull MessageDraft base;
	private final @NotNull Map<String, MessageDraft> placeholders;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var comp = base.toComponent(lang, chatVersion);
		var pattern = Pattern.compile(placeholders.keySet().stream()
				.filter(PLACEHOLDER_PATTERN.asMatchPredicate())
				.map(Pattern::quote)
				.collect(Collectors.joining("|", "%(", ")%")));
		comp = comp.replaceText(b -> b
				.match(pattern)
				.replacement((match, bu) -> getReplacementComponent(match.group(1), lang, chatVersion)));
		return this.replaceClickAndInsertion(comp, pattern, match -> getReplacement(match.group(1), lang, chatVersion));
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return LegacyMiniadventure.get().serialize(toComponent(lang, chatVersion));
	}
	
	private @NotNull Component getReplacementComponent(@NotNull String key, @NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var value = placeholders.get(key);
		if (value == null) {
			throw new IllegalStateException("Requested inexistent key");
		}
		return value.toComponent(lang, chatVersion);
	}
	
	private @NotNull String getReplacement(@NotNull String key, @NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var value = placeholders.get(key);
		if (value == null) {
			throw new IllegalStateException("Requested inexistent key");
		}
		return value.buildPlain(lang);
	}

}
