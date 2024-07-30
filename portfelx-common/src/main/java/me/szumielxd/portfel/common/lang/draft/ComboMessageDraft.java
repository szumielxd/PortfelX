package me.szumielxd.portfel.common.lang.draft;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.common.lang.Lang;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ComboMessageDraft extends MessageDraft {

	private final @NotNull MessageDraft base;
	private final @NotNull MessageDraft appended;
	private final boolean appendToLastChild;
	
	@Override
	protected @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		var comp = base.toComponent(lang, chatVersion);
		var toAppend = appended.toComponent(lang, chatVersion);
		if (appendToLastChild) {
			return appendToLastChild(comp, toAppend);
		}
		return comp.append(toAppend);
	}
	
	private @NotNull Component appendToLastChild(@NotNull Component comp, @NotNull Component toAppend) {
		Deque<Entry<Component, List<Component>>> stack = new LinkedList<>();
		List<Component> children;
		do {
			children = comp.children();
			stack.push(Map.entry(comp, new ArrayList<>(children)));
			comp = children.get(children.size() - 1);
		} while (!children.isEmpty());
		comp = comp.children(List.of(toAppend));
		while (!stack.isEmpty()) {
			var top = stack.pop();
			var childs = top.getValue();
			childs.set(childs.size() - 1, comp);
			comp = top.getKey().children(childs);
		}
		return comp;
	}
	
	
	
	

}
