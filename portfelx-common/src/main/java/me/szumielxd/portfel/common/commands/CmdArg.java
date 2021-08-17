package me.szumielxd.portfel.common.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class CmdArg {
	
	
	private final boolean flying;
	private final String prefix;
	private final LangKey name;
	private final LangKey description;
	private final LangKey argError;
	private final Function<String, Object> argParser;
	private final BiFunction<CommonSender, String[], List<String>> argCompletions;
	
	
	public CmdArg(@NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull Function<CommonSender, List<String>> argCompletions) {
		this(name, description, argError, argParser, (s, args) -> argCompletions.apply(s));
	}
	
	public CmdArg(@Nullable String prefix, @NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull Function<CommonSender, List<String>> argCompletions) {
		this(prefix, name, description, argError, argParser, (s, args) -> argCompletions.apply(s));
	}
	
	public CmdArg(@NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull BiFunction<CommonSender, String[], List<String>> argCompletions) {
		this(null, name, description, argError, argParser, argCompletions);
	}
	
	public CmdArg(@Nullable String prefix, @NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull BiFunction<CommonSender, String[], List<String>> argCompletions) {
		this(false, prefix, name, description, argError, argParser, argCompletions);
	}
	
	public CmdArg(boolean flying, @Nullable String prefix, @NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull BiFunction<CommonSender, String[], List<String>> argCompletions) {
		if (flying && (prefix == null || prefix.isEmpty())) throw new IllegalArgumentException("Flying argument must have not empty prefix");
		this.flying = flying;
		this.prefix = prefix == null ? null : prefix.toLowerCase();
		this.name = name;
		this.description = description;
		this.argError = argError;
		this.argParser = argParser;
		this.argCompletions = argCompletions;
	}
	
	
	/**
	 * Get displayed name of this argument.
	 * 
	 * @return language key to localized name
	 */
	public @NotNull LangKey getDisplay() {
		return this.name;
	}
	
	/**
	 * Get description of this argument.
	 * 
	 * @return language key to localized description
	 */
	public @NotNull LangKey getDescription() {
		return this.description;
	}
	
	/**
	 * Check whether this argument is optional.
	 * 
	 * @return true if this argument is optional, otherwise false
	 */
	public boolean isOptional() {
		return this.argError == null || this.isFlying();
	}
	
	/**
	 * Check whether this argument is not strict related to his position in list.
	 * 
	 * @return true if this argument is flying, otherwise false
	 */
	public boolean isFlying() {
		return this.flying;
	}
	
	/**
	 * Try to parse given text as valid argument.
	 * 
	 * @param arg text to parse
	 * @return parsed valid argument object or null if cannot be parsed
	 */
	public @Nullable Object parseArg(@NotNull String arg) {
		if (this.hasPrefix()) {
			if (!arg.toLowerCase().startsWith(this.getPrefix())) return null;
			return this.argParser.apply(arg.substring(this.getPrefix().length()));
		}
		return this.argParser.apply(arg);
	}
	
	/**
	 * Check whether this text can be parsed as valid argument.
	 * 
	 * @param arg text to check
	 * @return true if text can be parsed as valid object, otherwise false
	 */
	public boolean isValid(@NotNull String arg) {
		if (this.hasPrefix()) {
			return arg.toLowerCase().startsWith(this.getPrefix()) && this.argParser.apply(arg.substring(this.getPrefix().length())) != null;
		}
		return this.argParser.apply(arg) != null;
	}
	
	/**
	 * Get argument's prefix.
	 * 
	 * @return string prefix
	 */
	public @Nullable String getPrefix() {
		return this.prefix;
	}
	
	/**
	 * Check whether this argument has prefix.
	 * 
	 * @return true if prefix is not null, false otherwise
	 */
	public boolean hasPrefix() {
		return this.prefix != null;
	}
	
	/**
	 * Get message displayed on not valid text argument.
	 * 
	 * @param arg invalid argument
	 * @return translatable component with given replacements if this argument is not optional, otherwise null
	 */
	public @Nullable TranslatableComponent getArgError(@NotNull String arg) {
		return this.argError == null? null : this.argError.component(Component.text(arg));
	}
	
	/**
	 * Get message displayed on not valid text argument.
	 * 
	 * @param arg invalid argument
	 * @return translatable component with given replacements if this argument is not optional, otherwise null
	 */
	public @Nullable TranslatableComponent getArgError(@NotNull Component arg) {
		return this.argError == null? null : this.argError.component(RED, arg);
	}
	
	/**
	 * Get list of available tab completions for specified sender.
	 * 
	 * @param sender sender to calculate accessibility
	 * @return list of (maybe not all) text arguments available for this sender
	 */
	public @NotNull List<String> getTabCompletions(@NotNull CommonSender sender, @Nullable String... label) {
		if (this.hasPrefix()) {
			if (label.length == 0) return Collections.emptyList();
			String arg = label[label.length-1];
			if (!arg.toLowerCase().startsWith(getPrefix())) return Collections.emptyList();
			return this.argCompletions.apply(sender, MiscUtils.mergeArrays(MiscUtils.popArray(label), arg.substring(this.getPrefix().length())));
		}
		return this.argCompletions.apply(sender, label);
	}
	

}
