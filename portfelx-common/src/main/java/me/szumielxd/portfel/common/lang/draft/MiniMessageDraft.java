package me.szumielxd.portfel.common.lang.draft;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MiniMessageDraft extends MessageDraft {
	
	private final @NotNull String text;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return LegacyMiniadventure.get().deserialize(chatVersion, text);
	}

}
