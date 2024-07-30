package me.szumielxd.portfel.common.lang.draft;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LangMessageDraft extends MessageDraft {

	private final @NotNull LangKey key;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return LegacyMiniadventure.get().deserialize(chatVersion, lang.getValue(key));
	}

}
