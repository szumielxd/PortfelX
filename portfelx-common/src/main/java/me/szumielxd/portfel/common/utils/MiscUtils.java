package me.szumielxd.portfel.common.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.experimental.UtilityClass;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.MainLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UtilityClass
public class MiscUtils {
	
	@Deprecated
	public final @NotNull Component PREFIX = MiniMessage.miniMessage().deserialize("<b><aqua>[<dark_purple>P</dark_purple>]</aqua></b><dark_aqua> ");
	
	
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
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array, @NotNull T[] toAppend) {
		T[] newArray = Arrays.copyOf(array, array.length + toAppend.length);
		System.arraycopy(toAppend, 0, newArray, array.length, toAppend.length);
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
		if (amount > array.length) {
			amount = array.length;
		}
		return Arrays.copyOfRange(array, amount, array.length);
	}
	
	/**
	 * Join all {@link Component} elements with given delimer.
	 * 
	 * @param delimer the delimiter that separates each element
	 * @param elements the elements to join together
	 * @return a new String that is composed of the elements separated by the delimiter
	 */
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	 * Try to parse given text as MiniMessage component.
	 * 
	 * @param text text to parse
	 * @return parsed component
	 */
	@Deprecated
	public static @NotNull Component parseComponent(@NotNull String text) {
		if (Objects.requireNonNull(text, "text cannot be null").isEmpty()) {
			return Component.empty();
		}
		return MiniMessage.miniMessage().deserialize(text);
	}
	
	/**
	 * Try to parse given text as MiniMessage component.
	 * 
	 * @param text text to parse
	 * @return parsed component
	 */
	@Deprecated
	public static @NotNull Component parseComponent(@NotNull String text, @NotNull Pattern pattern, @NotNull Function<MatchResult, String> replacer) {
		Objects.requireNonNull(text, "text cannot be null");
		Objects.requireNonNull(pattern, "pattern cannot be null");
		Objects.requireNonNull(replacer, "replacer cannot be null");
		if (text.isEmpty()) {
			return Component.empty();
		}
		return MiniMessage.miniMessage().deserialize(replaceAll(pattern.matcher(text.replaceAll("&([0-9A-FK-ORa-fk-or])", "§$1")), replacer));
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
		duration /= 1000;
		int sec = (int) duration%60;
		duration /= 60;
		int min = (int) duration%60;
		duration /= 60;
		int hr = (int) duration%24;
		duration /= 24;
		int day = (int) duration%365;
		duration /= 365;
		StringBuilder time = new StringBuilder();
		if (duration > 0 || !removeZero) {
			time.append(MainLangKey.MAIN_VALUE_TIME_YEARS.draft(duration).buildPlain(lang)).append(" "); // years (1+)
			removeZero = false;
		}
		if (day > 0 || !removeZero) {
			time.append(MainLangKey.MAIN_VALUE_TIME_DAYS.draft(day).buildPlain(lang)).append(" "); // days (1-364)
			removeZero = false;
		}
		if (hr > 0 || !removeZero) {
			time.append(MainLangKey.MAIN_VALUE_TIME_HOURS.draft(hr).buildPlain(lang)).append(" "); // hours (1-23)
		}
		if (min > 0 || !removeZero) {
			time.append(MainLangKey.MAIN_VALUE_TIME_MINUTES.draft(min).buildPlain(lang)); // minutes (0-59)
		}
		
		if (day == 0 && hr == 0) {
			if (time.length() > 0) {
				time.append(" ");
			}
			time.append(MainLangKey.MAIN_VALUE_TIME_SECONDS.draft(sec).buildPlain(lang)); // seconds
		}
		return time.toString();
	}
	
	public static <E extends Enum<E>, T> EnumMap<E, T> mapOfEachEnum(Class<E> type, Function<E, T> valueGenerator) {
		return Stream.of(type.getEnumConstants())
				.collect(Collectors.toMap(
						Function.identity(),
						valueGenerator,
						(a, b) -> a,
						() -> new EnumMap<>(type)));
	}
	

}
