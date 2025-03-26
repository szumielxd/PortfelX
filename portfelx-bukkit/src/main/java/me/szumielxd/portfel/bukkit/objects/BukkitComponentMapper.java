package me.szumielxd.portfel.bukkit.objects;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;

import me.szumielxd.portfel.api.objects.ComponentMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class BukkitComponentMapper implements ComponentMapper<Component> {
	
	@Override
	public @NotNull Component jsonToComponent(@NotNull JsonElement json) {
		return GsonComponentSerializer.gson().deserializeFromTree(json);
	}

	@Override
	public @NotNull Component jsonStringToComponent(@NotNull String json) {
		return GsonComponentSerializer.gson().deserialize(json);
	}

	@Override
	public @NotNull JsonElement componentToJson(@NotNull Component component) {
		return GsonComponentSerializer.gson().serializeToTree(component);
	}

	@Override
	public @NotNull String componentToJsonString(@NotNull Component component) {
		return GsonComponentSerializer.gson().serialize(component);
	}

	@Override
	public @NotNull Component replaceText(@NotNull Component component, @NotNull String needle, String replacement) {
		return component.replaceText(b -> b.matchLiteral(needle).replacement(replacement));
	}

	@Override
	public @NotNull Component plainText(@NotNull String text) {
		return Component.text(text);
	}

	@Override
	public @NotNull Component parsePlaceholders(@NotNull Component comp, Map<String, Component> replacements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Component parsePlaceholdersInHover(@NotNull Component comp, Map<String, Component> replacements) {
		// TODO Auto-generated method stub
		return null;
	}

}
