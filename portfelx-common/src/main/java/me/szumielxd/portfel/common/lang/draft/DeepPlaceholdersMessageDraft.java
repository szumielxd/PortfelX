package me.szumielxd.portfel.common.lang.draft;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class DeepPlaceholdersMessageDraft extends MessageDraft {

	public static final @NotNull Pattern DEFAULT_PATTERN = Pattern.compile("%([a-z\\d]+)%", Pattern.CASE_INSENSITIVE);
	
	private final @NotNull MessageDraft base;
	private final @NotNull Pattern pattern;
	private final @NotNull Function<@NotNull String, @NotNull MessageDraft> placeholderReplacer;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return LegacyMiniadventure.get().deserialize(chatVersion, toMinimessageString(lang, chatVersion));
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return pattern.matcher(base.toMinimessageString(lang, chatVersion)).replaceAll(m -> {
			return Matcher.quoteReplacement(Optional.ofNullable(placeholderReplacer.apply(m.group()))
					.map(r -> r.toMinimessageString(lang, chatVersion))
					.orElse(m.group()));
		});
	}

}
