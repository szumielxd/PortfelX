package me.szumielxd.portfel.common.lang.draft;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ParameterizedMessageDraft extends MessageDraft {

	private final MessageDraft base;
	private final MessageDraft[] parameters;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var comp = base.toComponent(lang, chatVersion);
		var pattern = Pattern.compile(IntStream.range(0, parameters.length)
				.mapToObj(Integer::toString)
				.collect(Collectors.joining("|", "\\{(", ")\\}")));
		comp = comp.replaceText(b -> b.match(pattern)
				.replacement((match, bu) -> matchComponentArgument(lang, chatVersion, match.group(1))));
		return replaceClickAndInsertion(comp, pattern, match -> matchPlainArgument(lang, chatVersion, match.group(1)));
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return MiniMessage.miniMessage().serialize(toComponent(lang, chatVersion));
	}
	
	private String matchPlainArgument(@NotNull Lang lang, @NotNull ChatVersion chatVersion, String index) {
		return PlainTextComponentSerializer.plainText()
				.serialize(matchComponentArgument(lang, chatVersion, index));
	}
	
	private Component matchComponentArgument(@NotNull Lang lang, @NotNull ChatVersion chatVersion, String index) {
		return parameters[Integer.parseInt(index)].toComponent(lang, chatVersion);
	}

}
