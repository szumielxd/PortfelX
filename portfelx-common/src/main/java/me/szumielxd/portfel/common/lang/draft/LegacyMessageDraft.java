package me.szumielxd.portfel.common.lang.draft;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LegacyMessageDraft extends MessageDraft {
	
	static final @NotNull LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
			.hexColors()
			.character(LegacyComponentSerializer.SECTION_CHAR)
			.build();
	
	private final @NotNull String text;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return SERIALIZER.deserialize(text);
	}
	
	@Override
	public @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return LegacyMiniadventure.get().serialize(toComponent(lang, chatVersion));
	}

}
