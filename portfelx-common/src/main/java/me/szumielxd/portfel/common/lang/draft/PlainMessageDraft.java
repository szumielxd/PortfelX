package me.szumielxd.portfel.common.lang.draft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PlainMessageDraft extends MessageDraft {

	static final MessageDraft EMPTY = new PlainMessageDraft("");
	static final MessageDraft NEWLINE = new PlainMessageDraft("\n");
	static final MessageDraft SPACE = new PlainMessageDraft(" ");
	
	private final @Nullable Object base;

	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var obj = base;
		if (obj instanceof MessageDraft draft) {
			obj = draft.toComponent(lang, chatVersion);
		}
		if (obj instanceof ComponentLike comp) {
			return comp.asComponent();
		}
		return Component.text(String.valueOf(obj));
	}

}
