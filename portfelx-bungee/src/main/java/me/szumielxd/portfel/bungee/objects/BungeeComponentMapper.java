package me.szumielxd.portfel.bungee.objects;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import me.szumielxd.portfel.api.objects.ComponentMapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;

public class BungeeComponentMapper implements ComponentMapper<BaseComponent[]> {
	
	private static final @NotNull Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-z\\d]+)\\}", Pattern.CASE_INSENSITIVE);
	private static final @Nullable Gson bungeeGson;
	
	static {
		Gson bungee = null;
		try {
			Field f = ComponentSerializer.class.getDeclaredField("gson");
			f.setAccessible(true);
			bungee = (Gson) f.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			bungee = null;
		}
		bungeeGson = bungee;
	}

	@Override
	public @NotNull BaseComponent[] jsonToComponent(@NotNull JsonElement json) {
		if (bungeeGson != null) {
			if (json.isJsonArray()) {
				return bungeeGson.fromJson(json, BaseComponent[].class);
			} else {
				return toMany(bungeeGson.fromJson(json, BaseComponent.class));
			}
		}
		return ComponentSerializer.parse(json.toString());
	}

	@Override
	public @NotNull BaseComponent[] jsonStringToComponent(@NotNull String json) {
		return ComponentSerializer.parse(json);
	}

	@Override
	public @NotNull JsonElement componentToJson(@NotNull BaseComponent[] component) {
		if (bungeeGson != null) {
			return bungeeGson.toJsonTree(component, BaseComponent[].class);
		}
		return JsonParser.parseString(ComponentSerializer.toString(component));
	}

	@Override
	public @NotNull String componentToJsonString(@NotNull BaseComponent[] component) {
		return ComponentSerializer.toString(component);
	}
	
	public @NotNull BaseComponent replaceText(@NotNull BaseComponent component, @NotNull String needle, String replacement) {
		component = component.duplicate();
		if (component instanceof TextComponent text) {
			text.setText(text.getText().replace(needle, replacement));
		}
		component.setExtra(component.getExtra().stream()
				.map(e -> this.replaceText(e, needle, replacement))
				.toList());
		return component;
	}

	@Override
	public @NotNull BaseComponent[] replaceText(@NotNull BaseComponent[] component, @NotNull String needle, String replacement) {
		BaseComponent[] arr = new BaseComponent[component.length];
		for (int i = 0; i < component.length; i++) {
			arr[i] = this.replaceText(component[i], needle, replacement);
		}
		return arr;
	}

	@Override
	public @NotNull BaseComponent[] plainText(@NotNull String text) {
		return this.toMany(new TextComponent(text));
	}
	
	public @NotNull BaseComponent parsePlaceholders(@NotNull BaseComponent comp, Map<String, BaseComponent> replacements) {
		comp  = comp.duplicate();
		if (comp instanceof TextComponent text) {
			TextComponent toAppend = text.duplicate();
			toAppend.setExtra(List.of());
			int lastIndex = 0;
			Deque<BaseComponent> texts = new LinkedList<>();
			Matcher match = PLACEHOLDER_PATTERN.matcher(text.getText());
			while (match.find()) {
				var replacement = replacements.get(match.group(1));
				if (replacement != null) {
					String prev = text.getText().substring(lastIndex, match.start());
					if (!prev.isEmpty()) {
						toAppend = toAppend.duplicate();
						toAppend.setText(prev);
						texts.offer(toAppend);
					}
					texts.offer(replacement.duplicate());
					lastIndex = match.end();
				}
			}
			if (!texts.isEmpty()) {
				comp = new TextComponent(texts.toArray(BaseComponent[]::new));
			}
		}
		comp.setExtra(comp.getExtra().stream()
				.map(e -> this.parsePlaceholders(e, replacements))
				.toList());
		return comp;
	}

	@Override
	public @NotNull BaseComponent[] parsePlaceholders(@NotNull BaseComponent[] comp, Map<String, BaseComponent[]> replacements) {
		var replacementsMap = replacements.entrySet().stream()
			.collect(Collectors.toMap(Entry::getKey, e -> this.toOne(e.getValue())));
		BaseComponent[] arr = new BaseComponent[comp.length];
		for (int i = 0; i < comp.length; i++) {
			arr[i] = this.parsePlaceholders(comp[i], replacementsMap);
		}
		return arr;
	}
	
	public @NotNull BaseComponent parsePlaceholdersInHover(@NotNull BaseComponent comp, Map<String, BaseComponent> replacements) {
		if (comp.getHoverEvent() != null) {
			var hover = comp.getHoverEvent();
			List<Content> contents = comp.getHoverEvent().getContents().stream()
					.map(c -> {
						if (c instanceof Text t && t.getValue() instanceof BaseComponent[] base) {
							c = new Text(toMany(parsePlaceholders(this.toOne(base), replacements)));
						}
						return c;
					}).toList();
			comp.setHoverEvent(new HoverEvent(hover.getAction(), contents));
		}
		return comp;
	}

	@Override
	public @NotNull BaseComponent[] parsePlaceholdersInHover(@NotNull BaseComponent[] comp, Map<String, BaseComponent[]> replacements) {
		var replacementsMap = replacements.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> this.toOne(e.getValue())));
		BaseComponent[] arr = new BaseComponent[comp.length];
		for (int i = 0; i < comp.length; i++) {
			arr[i] = this.parsePlaceholdersInHover(comp[i], replacementsMap);
		}
		return arr;
	}
	
	private @NotNull BaseComponent toOne(@NotNull BaseComponent... components) {
		return components.length == 1 ? components[0] : new TextComponent(components);
	}
	
	private @NotNull BaseComponent[] toMany(@NotNull BaseComponent component) {
		return new BaseComponent[] { component };
	}

}
