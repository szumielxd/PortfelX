package me.szumielxd.portfel.common.commands;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.objects.CommonSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class CmdArg {
	
	
	private final LangKey name;
	private final LangKey description;
	private final LangKey argError;
	private final Function<String, Object> argParser;
	private final Function<CommonSender, List<String>> argCompletions;
	
	
	public CmdArg(@NotNull LangKey name, @NotNull LangKey description, @Nullable LangKey argError, @NotNull Function<String, Object> argParser, @NotNull Function<CommonSender, List<String>> argCompletions) {
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
		return this.argError == null;
	}
	
	/**
	 * Try to parse given text as valid argument.
	 * 
	 * @param arg text to parse
	 * @return parsed valid argument object or null if cannot be parsed
	 */
	public @Nullable Object parseArg(@NotNull String arg) {
		return this.argParser.apply(arg);
	}
	
	/**
	 * Check whether this text can be parsed as valid argument.
	 * 
	 * @param arg text to check
	 * @return true if text can be parsed as valid object, otherwise false
	 */
	public boolean isValid(@NotNull String arg) {
		return this.argParser.apply(arg) != null;
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
		return this.argError == null? null : this.argError.component(arg);
	}
	
	/**
	 * Get list of available tab completions for specified sender.
	 * 
	 * @param sender sender to calculate accessibility
	 * @return list of (maybe not all) text arguments available for this sender
	 */
	public @NotNull List<String> getTabCompletions(@NotNull CommonSender sender) {
		return this.argCompletions.apply(sender);
	}
	

}
