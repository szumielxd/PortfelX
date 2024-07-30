package me.szumielxd.portfel.common.lang.draft;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ParameterizedMessageDraft extends MessageDraft {

	private final MessageDraft base;
	private final MessageDraft[] parameters;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var comp = base.toComponent(lang, chatVersion);
		var pattern = Pattern.compile("\\{("
				+ IntStream.range(0, parameters.length)
						.mapToObj(Integer::toString)
						.collect(Collectors.joining("|")) + ")\\}");
		comp = comp.replaceText(b -> b.match(pattern)
				.replacement((match, bu) -> matchComponentArgument(lang, chatVersion, match.group(1))));
		return replaceClickAndInsertion(comp, pattern, match -> matchPlainArgument(lang, chatVersion, match.group(1)));
	}
	
	private String matchPlainArgument(@NotNull Lang lang, @NotNull ChatVersion chatVersion, String index) {
		return PlainTextComponentSerializer.plainText()
				.serialize(matchComponentArgument(lang, chatVersion, index));
	}
	
	private Component matchComponentArgument(@NotNull Lang lang, @NotNull ChatVersion chatVersion, String index) {
		return parameters[Integer.parseInt(index)].toComponent(lang, chatVersion);
	}
	
	private Component replaceClickAndInsertion(Component comp, Pattern pattern, Function<MatchResult, String> replacer) {
		var click = comp.clickEvent();
		if (click != null) {
			comp = comp.clickEvent(ClickEvent.clickEvent(click.action(), pattern.matcher(click.value()).replaceAll(replacer)));
		}
		if (comp.insertion() != null) {
			comp = comp.insertion(pattern.matcher(comp.insertion()).replaceAll(replacer));
		}
		return comp.children(comp.children().stream()
				.map(c -> replaceClickAndInsertion(c, pattern, replacer))
				.toList());
	}

}
