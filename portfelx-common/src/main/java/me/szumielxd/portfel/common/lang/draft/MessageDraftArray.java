package me.szumielxd.portfel.common.lang.draft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageDraftArray extends MessageDraft {

	private final @NotNull MessageDraft[] elements;
	
	MessageDraftArray(@NotNull MessageDraft... elements) {
		this.elements = elements;
	}
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return Stream.of(elements)
				.map(e -> e.toComponent(lang, chatVersion))
				.map(ComponentLike::asComponent)
				.collect(Component.toComponent());
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return MiniMessage.miniMessage().serialize(toComponent(lang, chatVersion));
	}
	
	public @NotNull List<MessageDraft> elements() {
		return Collections.unmodifiableList(Arrays.asList(elements));
	}

}
