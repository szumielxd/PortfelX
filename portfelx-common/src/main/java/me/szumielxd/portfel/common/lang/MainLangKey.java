package me.szumielxd.portfel.common.lang;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.lang.Lang.LangKey;

@Getter
public enum MainLangKey implements LangKey {
	
	ERROR_COMMAND_EXECUTION("error.command-execution", "<dark_red>No way! An error occured while attempting to perform this command! See console for any usefull information."),
	ERROR_COMMAND_USER_NOT_LOADED("error.user-not-loaded", "<red>Sorry, but your data is not loaded. Are you a ghost?!"),
	ERROR_COMMAND_PERMISSION("error.command-permission", "<red>No! You can't just run this command, you need the access!"),
	ERROR_COMMAND_CONSOLE_ONLY("error.command-console-only", "<red>You can do many things, but you must be a console to execute this command!"),
	ERROR_COMMAND_PLAYERS_ONLY("error.command-players-only", "<red>You're just only a console! This command requires something more... a PLAYER..."),
	
	COMMAND_USAGE_TITLE("command.usage.title", "<dark_purple>Command Usage - <light_purple>{0}"),
	COMMAND_USAGE_DESCRIPTION("command.usage.description", "<light_pink>> <gray>{0}"),
	COMMAND_USAGE_ALIASES("command.usage.aliases", "<dark_purple>Aliases"),
	COMMAND_USAGE_ALIAS_FORMAT("command.usage.alias.format", "<light_purple>- <gray>{0}"),
	COMMAND_USAGE_ARGUMENTS("command.usage.arguments", "<dark_purple>Arguments"),
	COMMAND_USAGE_ARGUMENT_FORMAT("command.usage.argument.format", "<light_purple>- {0} -> <gray>{1}"),
	COMMAND_USAGE_ARGUMENT_OPTIONAL("command.usage.argument.optional", "[{0}<{1}>]"),
	COMMAND_USAGE_ARGUMENT_MANDATORY("command.usage.argument.mandatory", "{0}<{1}>"),

	COMMAND_MAIN_RUNNING("command.main.running", "<dark_purple>Running <light_purple>{0}</light_purple>."),
	COMMAND_MAIN_USE("command.main.use", "<dark_aqua>Use <aqua><hover:show_text:{1}><click:run_command:{0}><insert:{0}>{0}</insert><click></hover></aqua> to view available commands."),
	COMMAND_MAIN_SUBCOMMANDS_LINE("command.main.subcommands.line", "<light_purple>> <hover:show_text:{2}><click:run_command:{0}><insert:{0}><aqua>{0}{1]"),
	
	COMMAND_SUBCOMMANDS_TITLE("command.subcommands.title", "<light_purple>{0} Sub Commands: <gray>(/{1}...)"),
	COMMAND_SUBCOMMANDS_LINE_WITHARGS("command.subcommands.line.with-args", "<hover:show_text:{2}><insert:{3}><click:run_command:{3}><light_purple><bold>> </bold><aqua>{0} <darg_purple>-</dark_purple> {1}"),
	COMMAND_SUBCOMMANDS_LINE_WITHOUTARGS("command.subcommands.line.without-args", "<hover:show_text:{1}><insert:{2}><click:run_command:{2}><light_purple><bold>> </bold><aqua>{0}"),
	COMMAND_SUBCOMMANDS_EXECUTE("command.subcommands.execution", "<dark_gray>» <gray>Click to execute this command"),
	COMMAND_SUBCOMMANDS_INSERT("command.subcommands.insertion", "<dark_gray>» <gray>Click+Shift to insert this command"),
	
	COMMAND_HELP_DESCRIPTION("command.help.decription", "List all available portfel subcommands."),
	
	MAIN_VALUE_TIME_SECONDS("main.value.time.seconds", "{0}s"),
	MAIN_VALUE_TIME_MINUTES("main.value.time.minutes", "{0}m"),
	MAIN_VALUE_TIME_HOURS("main.value.time.hours", "{0}h"),
	MAIN_VALUE_TIME_DAYS("main.value.time.days", "{0}d"),
	MAIN_VALUE_TIME_YEARS("main.value.time.years", "{0}y"),
	
	// UNMODIFIABLE
	EMPTY("empty", "", false),
	PREFIX("prefix", "<b><aqua>[<dark_purple>P</dark_purple>]</aqua></b><dark_aqua> ", false),
	
	;// END,
	
	private final String path;
	private final String defString;
	private final boolean modifiable;
	

	/**
	 * Lang enum constructor.
	 * 
	 * @param path The string path.
	 * @param start The default string.
	 */
	private MainLangKey(String path, String defString) {
		this(path, defString, true);
	}
	
	/**
	 * Lang enum constructor.
	 * 
	 * @param path The string path.
	 * @param start The default string.
	 * @param whether value should be loaded from file
	 */
	private MainLangKey(@NotNull String path, @NotNull String defString, boolean modifiable) {
		this.path = path;
		this.defString = defString;
		this.modifiable = modifiable;
	}

}
