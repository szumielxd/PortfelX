package me.szumielxd.portfel.api.objects;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;

import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public interface ComponentMapper<C> {
	
	public @NotNull C jsonToComponent(@NotNull JsonElement json);
	
	public @NotNull C jsonStringToComponent(@NotNull String json);

	public @NotNull JsonElement componentToJson(@NotNull C component);

	public @NotNull String componentToJsonString(@NotNull C component);
	
	public @NotNull C replaceText(@NotNull C component, @NotNull String needle, String replacement);
	
	public @NotNull C plainText(@NotNull String text);
	
	public default @NotNull C empty() {
		return plainText("");
	}
	
	public @NotNull C parsePlaceholders(@NotNull C comp, Map<String, C> replacements);
	
	public @NotNull C parsePlaceholdersInHover(@NotNull C comp, Map<String, C> replacements);
	
	public default @NotNull C fullyParsePlaceholders(@NotNull C component, Map<String, C> replacements) {
		return parsePlaceholders(parsePlaceholdersInHover(component, replacements), replacements);
	}
	
	public default @NotNull VersionableObject<C> replacePlaceholders(@NotNull VersionableObject<C> message, Map<String, C> replacements) {
		return message.map(c -> fullyParsePlaceholders(c, replacements));
	}
	
	public default @NotNull VersionableObject<C> replacePlainPlaceholders(@NotNull VersionableObject<C> message, Map<String, String> replacements) {
		return replacePlaceholders(message, replacements.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> plainText(e.getValue()))));
	}
	
	public default KyoriComponentMapper<C> kyori() {
		return new KyoriComponentMapper<>(this);
	}
	
	@RequiredArgsConstructor
	public static class KyoriComponentMapper<C> {
		
		private @NotNull ComponentMapper<C> parentMapper;
		
		public @NotNull C kyoriToComponent(@NotNull Component component) {
			return parentMapper.jsonToComponent(GsonComponentSerializer.gson().serializeToTree(component));
		}
		
		public @NotNull Component componentToKyori(@NotNull C component) {
			return GsonComponentSerializer.gson().deserializeFromTree(parentMapper.componentToJson(component));
		}
		
		public @NotNull VersionableObject<C> parseLegacyMessage(@NotNull String message) {
			return LegacyMiniadventure.get().deserialize(message)
					.map(this::kyoriToComponent);
		}
		
	}
	

}
