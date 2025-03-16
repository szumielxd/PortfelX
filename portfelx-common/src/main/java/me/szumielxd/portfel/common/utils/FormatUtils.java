package me.szumielxd.portfel.common.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.MainLangKey;

@UtilityClass
public class FormatUtils {
	

	
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
	@Contract("null -> null")
	public static @Nullable String firstToUpper(@Nullable String text, boolean strict) {
		if (text == null) return null;
		if (text.length() == 0) return text;
		return text.substring(0, 1).toUpperCase() + (strict? text.toLowerCase() : text).substring(1, text.length());
	}
	
	/**
	 * Format duration given in milliseconds as localized text. 
	 * 
	 * @param lang language used to localize
	 * @param duration duration to format
	 * @param removeZero if true values equal to zero will be ignored (except minutes)
	 * @return formatted text
	 */
	public static @NotNull String formatDuration(@NotNull Lang lang, long duration, boolean removeZero) {
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

}
