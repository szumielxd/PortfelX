package me.szumielxd.portfel.common.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

@UtilityClass
public class ComponentUtils {
	
	
	public static final @NotNull Pattern NEWLINE_PATTERN = Pattern.compile("\\n");
	
	
	public @NotNull Component[] split(Component comp, Pattern pattern) {
		var components = new LinkedList<>();
		if (comp instanceof TextComponent text) {
			String[] elements = pattern.split(text.content(), -1);
			int lastElementIndex = elements.length - 1;
			for (int i = 0; i < lastElementIndex; i++) {
				components.add(text.content(elements[i]).children(List.of()));
			}
			comp = text.content(elements[lastElementIndex]);
		}
		Component[][] splitChilds = comp.children().stream()
				.map(c -> split(c, pattern))
				.toArray(Component[][]::new);
		List<Component> newChilds = new LinkedList<>();
		for (int i = 0; i < splitChilds.length; i++) {
			newChilds.add(splitChilds[i][0]);
			for (int j = 1; j < splitChilds[i].length; j++) { // something was split
				components.add(comp.children(newChilds));
				comp = Component.text("", comp.style());
				newChilds.clear();
				newChilds.add(splitChilds[i][j]);
			}
		}
		components.add(comp.children(newChilds));
		return components.toArray(Component[]::new);
	}
	
	
	public @NotNull Component[] splitByNewline(Component comp) {
		return split(comp, NEWLINE_PATTERN);
	}
	

}
