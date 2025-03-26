package me.szumielxd.portfel.common.lang.draft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PlainMessageDraft extends MessageDraft {
	
	static final PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.plainText();

	static final MessageDraft EMPTY = new PlainMessageDraft("");
	static final MessageDraft NEWLINE = new PlainMessageDraft("\n");
	static final MessageDraft SPACE = new PlainMessageDraft(" ");
	
	private final @Nullable Object base;

	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		if (base instanceof MessageDraft draft) {
			return draft.toComponent(lang, chatVersion);
		}
		if (base instanceof ComponentLike comp) {
			return comp.asComponent();
		}
		return Component.text(String.valueOf(base));
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		if (base instanceof MessageDraft draft) {
			return draft.toMinimessageString(lang, chatVersion);
		}
		if (base instanceof ComponentLike comp) {
			return LegacyMiniadventure.get().serialize(comp.asComponent());
		}
		return LegacyMiniadventure.get().escapeTags(String.valueOf(base));
	}

}
