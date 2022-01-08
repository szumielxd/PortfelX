package me.szumielxd.portfel.common.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class MiscUtils {
	
	
	/**
	 * Check if given UUID is related to premium account.
	 * 
	 * @param uuid unique ID to check
	 * @return true if this is premium UUID, otherwise false
	 */
	public static boolean isOnlineModeUUID(@Nullable UUID uuid) {
		if(uuid != null && uuid.version() == 4) return true;
		return false;
	}
	
	/**
	 * Append null element to end of the first array.
	 * 
	 * @param array the array
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array) {
		return mergeArrays(array, null);
	}
	
	/**
	 * Append given <b>toAppend</b> element to end of the first array.
	 * 
	 * @param array first array
	 * @param toAppend element to append
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array, @Nullable T toAppend) {
		return mergeArrays(array, Arrays.asList(toAppend).toArray(array.clone()));
	}
	
	/**
	 * Append given <b>toAppend</b> array to end of the first array.
	 * 
	 * @param array first array
	 * @param toAppend array to append
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array, @Nullable T[] toAppend) {
		if (toAppend == null) return Arrays.copyOf(array, array.length+1);
		T[] newArray = Arrays.copyOf(array, array.length+toAppend.length);
		for (int i = 0; i < toAppend.length; i++) {
			newArray[i+array.length] = toAppend[i];
		}
		return newArray;
	}
	
	/**
	 * remove first element from the array.
	 * 
	 * @param array the array
	 * @return new modified array
	 */
	public static <T> @NotNull T[] popArray(@NotNull T[] array) {
		return popArray(array, 1);
	}
	
	/**
	 * remove <b>amount</b> of first elements from the array.
	 * 
	 * @param array the array
	 * @param amount amount of elements to remove
	 * @return new modified array
	 */
	public static <T> @NotNull T[] popArray(@NotNull T[] array, int amount) {
		return Arrays.copyOfRange(array, amount, array.length);
	}
	
	/**
	 * Join all {@link Component} elements with given delimer.
	 * 
	 * @param delimer the delimiter that separates each element
	 * @param elements the elements to join together
	 * @return a new String that is composed of the elements separated by the delimiter
	 */
	public static @NotNull Component join(@NotNull String delimer, @NotNull Component... elements) {
		return join(Component.text(delimer), elements);
	}
	
	/**
	 * Join all {@link Component} elements with given delimer.
	 * 
	 * @param delimer the delimiter that separates each element
	 * @param elements the elements to join together
	 * @return a new String that is composed of the elements separated by the delimiter
	 */
	public static @NotNull Component join(@NotNull String delimer, @NotNull List<Component> elements) {
		return join(Component.text(delimer), elements);
	}
	
	/**
	 * Join all {@link Component} elements with given delimer.
	 * 
	 * @param delimer the delimiter that separates each element
	 * @param elements the elements to join together
	 * @return a new String that is composed of the elements separated by the delimiter
	 */
	public static @NotNull Component join(@NotNull Component delimer, @NotNull Component... elements) {
		return join(delimer, Arrays.asList(elements));
	}
	
	/**
	 * Join all {@link Component} elements with given delimer.
	 * 
	 * @param delimer the delimiter that separates each element
	 * @param elements the elements to join together
	 * @return a new String that is composed of the elements separated by the delimiter
	 */
	public static @NotNull Component join(@NotNull Component delimer, @NotNull List<Component> elements) {
		Component comp = Component.empty();
		List<Component> childs = new ArrayList<>();
		if (!elements.isEmpty()) {
			elements.forEach(e -> childs.addAll(Arrays.asList(delimer, e)));
			childs.remove(0);
		}
		return comp.children(childs);
	}
	
	/**
	 * Capitalize first letter of string.
	 * 
	 * @param text text to transform
	 * @return transformed string
	 */
	public static @Nullable String firstToUpper(@Nullable String text) {
		return firstToUpper(text, false);
	}
	
	/**
	 * Capitalize first letter of string and additionally lowercase other letters.
	 * 
	 * @param text text to transform
	 * @param strict whether to lowercase other letters
	 * @return transformed string
	 */
	public static @Nullable String firstToUpper(@Nullable String text, boolean strict) {
		if (text == null) return null;
		if (text.length() == 0) return text;
		return text.substring(0, 1).toUpperCase() + (strict? text.toLowerCase() : text).substring(1, text.length());
	}
	
	/**
	 * Get <b>untranslated</b> {@link Component} representation of {@link CmdArg} with default argument (LIGHT_GRAY) and brackets (DARK_GRAY) colors.
	 * 
	 * @param arg the argument
	 * @param argColor color of argument display name
	 * @param bracketsColor color of brackets
	 * @return component representation of argument
	 */
	public static @NotNull Component argToComponent(@NotNull CmdArg arg) {
		return argToComponent(arg, GRAY, DARK_GRAY);
	}
	
	/**
	 * Get <b>untranslated</b> {@link Component} representation of {@link CmdArg}.
	 * 
	 * @param arg the argument
	 * @param argColor color of argument display name
	 * @param bracketsColor color of brackets
	 * @return component representation of argument
	 */
	public static @NotNull Component argToComponent(@NotNull CmdArg arg, @Nullable TextColor argColor, @Nullable TextColor bracketsColor) {
		String prefix = (arg.isOptional() ? "[" : "") + (arg.hasPrefix()? arg.getPrefix() : "") + "<";
		String suffix = arg.isOptional() ? ">]" : ">";
		return Component.empty().children(Arrays.asList(Component.text(prefix, bracketsColor),
				arg.getDisplay().component(argColor), Component.text(suffix, bracketsColor)));
	}
	
	/**
	 * Get plain uncolored text representation of {@link CmdArg}.
	 * 
	 * @param lang language used to translate {@link LangKey} components
	 * @param arg the argument
	 * @return uncolored string representation of argument
	 */
	public static @NotNull String argToCleanText(@NotNull Lang lang, @NotNull CmdArg arg) {
		return argToPlainText(lang, arg, null, null);
	}
	
	/**
	 * Get legacy color text representation of {@link CmdArg} with default argument (LIGHT_GRAY) and brackets (DARK_GRAY) colors.
	 * 
	 * @param lang language used to translate {@link LangKey} components
	 * @param arg the argument
	 * @return string visual representation of argument
	 */
	public static @NotNull String argToPlainText(@NotNull Lang lang, @NotNull CmdArg arg) {
		return argToPlainText(lang, arg, ChatColor.GRAY, ChatColor.DARK_GRAY);
	}
	
	/**
	 * Get legacy color text representation of {@link CmdArg}.
	 * 
	 * @param lang language used to translate {@link LangKey} components
	 * @param arg the argument
	 * @param argColor color of argument display name
	 * @param bracketsColor color of brackets
	 * @return string visual representation of argument
	 */
	public static @NotNull String argToPlainText(@NotNull Lang lang, @NotNull CmdArg arg, @Nullable ChatColor argColor, @Nullable ChatColor bracketsColor) {
		String prefix = (bracketsColor!=null ? bracketsColor.toString() : "") + (arg.isOptional() ? "[" : "") + (arg.hasPrefix()? arg.getPrefix() : "") + "<" + (argColor!=null ? argColor.toString() : "");
		String suffix = (bracketsColor!=null ? bracketsColor.toString() : "") + (arg.isOptional() ? ">]" : ">");
		return prefix + lang.text(arg.getDisplay()) + suffix;
	}
	
	/**
	 * Build interactive command usage {@link Component} for given command.
	 * 
	 * @param baseMessage message to apply changes
	 * @param fullCommand string representation of usage string
	 * @param command the command used to get data
	 * @return interactive {@link Component} with insertion and hover and click events
	 */
	public static @NotNull Component buildCommandUsage(@NotNull Component baseMessage, String fullCommand, SimpleCommand command) {
		Component hover = Component.text(fullCommand, GREEN);
		//description
		hover = hover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_DESCRIPTION.component(AQUA))
				.append(Component.space())
				.append(command.getDescription().component(GRAY));
		//aliases
		if (command.getAliases().length > 0) hover = hover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_ALIASES.component(AQUA))
				.append(Component.space())
				.append(Component.text(String.join(", ", command.getAliases()), GRAY));
		//permission
		hover = hover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_PERMISSION.component(AQUA))
				.append(Component.space())
				.append(Component.text(command.getPermission(), GRAY));
		hover = hover.append(Component.newline());
		return bindCommand(baseMessage, fullCommand);
	}
	
	public static @NotNull Component bindCommand(@NotNull Component baseMessage, String fullCommand) {
		Component hover = Component.text("» ", DARK_GRAY).append(LangKey.COMMAND_SUBCOMMANDS_EXECUTE.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_SUBCOMMANDS_INSERT.component(GRAY));
		if (baseMessage.hoverEvent() != null) {
			if (baseMessage.hoverEvent().action().equals(Action.SHOW_TEXT)) {
				baseMessage = baseMessage.hoverEvent(((Component) baseMessage.hoverEvent().value()).append(Component.newline()).append(hover));
			}
		} else {
			baseMessage = baseMessage.hoverEvent(hover);
		}
		return baseMessage.clickEvent(ClickEvent.runCommand(fullCommand)).insertion(fullCommand);
	}
	
	public static @NotNull Component extendedCommandUsage(@NotNull SimpleCommand command) {
		Component result = Portfel.PREFIX.append(LangKey.COMMAND_USAGE_TITLE.component(DARK_PURPLE, Component.text(command.getName(), LIGHT_PURPLE)));
		result = result.append(Component.newline()).append(Portfel.PREFIX).append(Component.text("> ", LIGHT_PURPLE)).append(command.getDescription().component(GRAY));
		if (command.getAliases().length > 0) {
			result = result.append(Component.newline()).append(Portfel.PREFIX).append(LangKey.COMMAND_USAGE_ALIASES.component(DARK_PURPLE));
			for (String alias : command.getAliases()) {
				result = result.append(Component.newline()).append(Portfel.PREFIX).append(Component.text("- ", LIGHT_PURPLE)).append(Component.text(alias, GRAY));
			}
		}
		if (!command.getArgs().isEmpty()) {
			result = result.append(Component.newline()).append(Portfel.PREFIX).append(LangKey.COMMAND_USAGE_ARGUMENTS.component(DARK_PURPLE));
			for (CmdArg arg : command.getArgs()) {
				result = result.append(Component.newline()).append(Portfel.PREFIX).append(Component.text("- ", LIGHT_PURPLE)).append(MiscUtils.argToComponent(arg))
						.append(Component.text(" -> ")).append(arg.getDescription().component(GRAY));
			}
		}
		return result;
	}
	
	/**
	 * Try to parse given text as JSON component, on fail fallback to plain legacy format.
	 * 
	 * @param text text to parse
	 * @return parsed component
	 */
	public static @NotNull Component parseComponent(@NotNull String text) {
		if (Objects.requireNonNull(text, "text cannot be null").isEmpty()) return Component.empty();
		try {
			JsonObject json = new Gson().fromJson(text, JsonObject.class);
			return GsonComponentSerializer.gson().deserializeFromTree(json);
		} catch (JsonSyntaxException e) {
			return LegacyComponentSerializer.legacySection().deserialize(text.replaceAll("&([0-9A-FK-ORa-fk-or])", "§$1"));
		}
	}
	
	/**
	 * Try to parse given text as JSON component, on fail fallback to plain legacy format.
	 * 
	 * @param text text to parse
	 * @return parsed component
	 */
	public static @NotNull Component parseComponent(@NotNull String text, @NotNull Pattern pattern, @NotNull Function<MatchResult, String> replacer) {
		Objects.requireNonNull(text, "text cannot be null");
		Objects.requireNonNull(pattern, "pattern cannot be null");
		Objects.requireNonNull(replacer, "replacer cannot be null");
		if (text.isEmpty()) return Component.empty();
		try {
			JsonObject json = new Gson().fromJson(text, JsonObject.class);
			replaceTextInJson(json, pattern, replacer);
			return GsonComponentSerializer.gson().deserializeFromTree(json);
		} catch (JsonSyntaxException e) {
			return LegacyComponentSerializer.legacySection().deserialize(replaceAll(pattern.matcher(text.replaceAll("&([0-9A-FK-ORa-fk-or])", "§$1")), replacer));
		}
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param json json object
	 * @param pattern pattern to replace
	 * @param replacer replacement function
	 */
	public static void replaceTextInJson(@NotNull JsonObject json, @NotNull Pattern pattern, @NotNull Function<MatchResult, String> replacer) {
		Objects.requireNonNull(json, "json cannot be null");
		Objects.requireNonNull(pattern, "pattern cannot be null");
		Objects.requireNonNull(replacer, "replacer cannot be null");
		json.entrySet().stream().forEach(e -> {
			if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
				String val = e.getValue().getAsString();
				String newVal = replaceAll(pattern.matcher(val), replacer);
				if (!newVal.equals(val)) json.addProperty(e.getKey(), newVal);
			} else if (e.getValue().isJsonObject()) {
				replaceTextInJson(e.getValue().getAsJsonObject(), pattern, replacer);
			} else if (e.getValue().isJsonArray()) {
				replaceTextInJson(e.getValue().getAsJsonArray(), pattern, replacer);
			}
		});
	}
	
	/**
	 * Replace provided pattern with result of replacer function in given JSON.
	 * 
	 * @param json json array
	 * @param pattern pattern to replace
	 * @param replacer replacement function
	 */
	public static void replaceTextInJson(@NotNull JsonArray json, Pattern pattern, Function<MatchResult, String> replacer) {
		IntStream.range(0, json.size()).boxed().map(i -> new SimpleEntry<>(i, json.get(i))).forEach(e -> {
			if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
				String val = e.getValue().getAsString();
				String newVal = replaceAll(pattern.matcher(val), replacer);
				if (!newVal.equals(val)) json.set(e.getKey(), new JsonPrimitive(newVal));
			} else if (e.getValue().isJsonObject()) {
				replaceTextInJson(e.getValue().getAsJsonObject(), pattern, replacer);
			} else if (e.getValue().isJsonArray()) {
				replaceTextInJson(e.getValue().getAsJsonArray(), pattern, replacer);
			}
		});
	}
	
	public static String replaceAll(Matcher matcher, Function<MatchResult, String> replacement) {
		StringBuffer sb = new StringBuffer(); 
		while(matcher.find()){ 
		    String repl = replacement.apply(matcher);
		    matcher.appendReplacement(sb, repl); 
		} 
		matcher.appendTail(sb); 
		return sb.toString();
	}
	
	/**
	 * Format duration given in milliseconds as localized text. 
	 * 
	 * @param lang language used to localize
	 * @param duration duration to format
	 * @param removeZero if true values equal to zero will be ignored (except minutes)
	 * @return formatted text
	 */
	public static String formatDuration(Lang lang, long duration, boolean removeZero) {
		duration /= 60000;
		long min = duration%60;
		duration /= 60;
		long hr = duration%24;
		duration /= 24;
		long day = duration%365;
		duration /= 365;
		StringBuilder time = new StringBuilder();
		if (duration > 0 || !removeZero) {
			time.append(lang.text(LangKey.MAIN_VALUE_TIME_YEARS, duration)).append(" "); // years (1+)
			removeZero = false;
		}
		if (day > 0 || !removeZero) {
			time.append(lang.text(LangKey.MAIN_VALUE_TIME_DAYS, day)).append(" "); // days (1-364)
			removeZero = false;
		}
		if (hr > 0 || !removeZero) {
			time.append(lang.text(LangKey.MAIN_VALUE_TIME_HOURS, hr)).append(" "); // hours (1-23)
		}
		time.append(lang.text(LangKey.MAIN_VALUE_TIME_MINUTES, min)); // minutes (0-59)
		return time.toString();
	}
	

}
