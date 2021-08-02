package me.szumielxd.portfel.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import me.szumielxd.portfel.common.Config.ConfigKey;
import me.szumielxd.portfel.common.objects.CommonPlayer;
import me.szumielxd.portfel.common.objects.CommonSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.Translator;

public class Lang {
	
	
	public static enum LangKey {
		
		
		
		ERROR_COMMAND_PERMISSION("error.command-permission", "No! You can't just run this command, you need the access!"),
		ERROR_COMMAND_CONSOLE_ONLY("error.command-console-only", "You can do many things, but you must be a console to execute this command!"),
		ERROR_COMMAND_PLAYERS_ONLY("error.command-players-only", "You're just only a console! This command requires something more... a PLAYER..."),
		
		COMMAND_ARGTYPES_USER_DISPLAY("command.arg-types.user.display", "user"),
		COMMAND_ARGTYPES_USER_DESCRIPTION("command.arg-types.user.description", "user representated by name or unique id"),
		COMMAND_ARGTYPES_SERVERNAME_DISPLAY("command.arg-types.servername.display", "serverName"),
		COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION("command.arg-types.servername.description", "user-friendly text representation of server instance (not the same as bungee serverName)"),
		
		COMMAND_HELP_DESCRIPTION("command.help.decription", "List all available portfel subcommands."),
		
		COMMAND_SYSTEM_DESCRIPTION("command.system.description", "All portfel setup related commands."),
		
		COMMAND_SYSTEM_REGISTERSERVER_DESCRIPTION("command.system.registerserver.description", "Register your current server with given friendly name."),
		COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT("command.system.registerserver.timeout", "Are you sure, server you want to register has actual version of Portfel? He's not responding..."),
		COMMAND_SYSTEM_REGISTERSERVER_ALREADY("command.system.registerserver.already", "Is there any intelligent reason to register already registered server? Pro Tip: Check ID {0}."),
		COMMAND_SYSTEM_REGISTERSERVER_SUCCESS("command.system.registerserver.success", "You did it! You registered new portfel server with friendly name {0} and id {1}!"),
		COMMAND_SYSTEM_REGISTERSERVER_ERROR("command.system.registerserver.error", "This... This was very interesting. Server returned an unknown response."),
		COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_NEEDED("command.system.registerserver.servername-needed", "We need a user friendly and memorable text for use as shorthand of server ID. Please provide id."),
		COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY("command.system.registerserver.servername-already", "Did you remember this shorthand is already in use for another server?"),
		
		COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION("command.system.unregisterserver.description", "Unregister given server."),
		
		COMMAND_SUBCOMMANDS_TITLE("command.subcommands.title", "{0} Sub Commands:"),
		COMMAND_SUBCOMMANDS_EXECUTE("command.subcommands.execution", "Click to execute this command"),
		COMMAND_SUBCOMMANDS_INSERT("command.subcommands.insertion", "Shift+Click to insert this command"),
		
		COMMAND_MAIN_RUNNING("command.main.running", "Running {0}."),
		COMMAND_MAIN_USE("command.main.use", "Use {0} to view available commands."),
		
		MAIN_VALUENAME_DESCRIPTION("main.value-name.description", "Description:"),
		MAIN_VALUENAME_ALIASES("main.value-name.aliases", "Aliases:"),
		MAIN_VALUENAME_ENABLED("main.value-name.enabled", "Enabled:"),
		MAIN_VALUENAME_AUTHORS("main.value-name.aliases", "Authors:"),
		MAIN_VALUENAME_PERMISSION("main.value-name.permission", "Permission:"),
		
		MAIN_VALUE_TRUE("main.value.true", "true"),
		MAIN_VALUE_FALSE("main.value.false", "false"),
		MAIN_VALUE_YES("main.value.yes", "yes"),
		MAIN_VALUE_NO("main.value.no", "no"),
		
		MAIN_MESSAGE_INSERTION("main.message.insertion", "Click to insert the {0}."),
		
		// UNMODIFIABLE
		EMPTY("", "", false),
		
		;// END,
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
		private final String path;
		private final String defString;
		private final boolean modifiable;
		
		private static final Map<String, LangKey> KEYS_BY_PATH = new HashMap<>();
		
		
		static {
			for (LangKey key : LangKey.values()) {
				KEYS_BY_PATH.put(key.path, key);
			}
		}
		

		/**
		 * Lang enum constructor.
		 * 
		 * @param path The string path.
		 * @param start The default string.
		 */
		LangKey(String path, String defString) {
			this(path, defString, true);
		}
		
		/**
		 * Lang enum constructor.
		 * 
		 * @param path The string path.
		 * @param start The default string.
		 * @param modifiable whether value should be loaded from file
		 */
		LangKey(String path, String defString, boolean modifiable) {
			this.path = path;
			this.defString = defString;
			this.modifiable = modifiable;
		}
		
		
		/**
		 * Get the String path.
		 * 
		 * @return The String patch.
		 */
		public String getPath() {
			return path;
		}
		
		public String getDefString() {
			return this.defString;
		}
		
		public boolean isModifiable() {
			return this.modifiable;
		}
		
		public @NotNull TranslatableComponent component() {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath());
		}
		
		public @NotNull TranslatableComponent component(@NotNull ComponentLike... args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), args);
		}
		
		public @NotNull TranslatableComponent component(@NotNull List<ComponentLike> args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), args);
		}
		
		public @NotNull TranslatableComponent component(@NotNull Style style) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), style);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color);
		}
		
		public @NotNull TranslatableComponent component(@NotNull Style style, @NotNull ComponentLike... args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), style, args);
		}
		
		public @NotNull TranslatableComponent component(@NotNull Style style, @NotNull List<ComponentLike> args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), style, args);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull ComponentLike... args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, args);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull List<ComponentLike> args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, args);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull TextDecoration... decorations) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, decorations);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull Set<TextDecoration> decorations) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, decorations);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull Set<TextDecoration> decorations, @NotNull ComponentLike... args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, decorations, args);
		}
		
		public @NotNull TranslatableComponent component(@Nullable TextColor color, @NotNull Set<TextDecoration> decorations, @NotNull List<ComponentLike> args) {
			return Component.translatable(TRANSLATABLE_PREFIX+this.getPath(), color, decorations, args);
		}
		
		
		public static JsonObject asJsonObject() {
			JsonObject json = new JsonObject();
			for (LangKey key : LangKey.values()) {
				if (key.isModifiable()) json.addProperty(key.getPath(), key.getDefString());
			}
			return json;
		}
		
		public static @Nullable LangKey getByPath(@Nullable String path) {
			return KEYS_BY_PATH.get(path);
		}
		
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private static Map<Locale, Lang> LANG_BY_LOCALE = new HashMap<>();
	private static Locale DEFAULT_LOCALE;
	private static final Pattern FILE_PATTERN = Pattern.compile("messages-[a-z]{2}(_[A-Z]{2})?\\.json");
	private static final Gson GSON_SERIALIZER = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	public static final String TRANSLATABLE_PREFIX = "portfel.";
	
	
	/**
	 * Setup Lang instances. Internal use only
	 * @param locales directory
	 */
	public static void load(@NotNull File dir) {
		if (dir.isFile() && dir.exists()) dir.delete();
		if (!dir.exists()) dir.mkdirs();
		File[] files = dir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
		DEFAULT_LOCALE = Translator.parseLocale(Config.getString(ConfigKey.LANG_DEFAULT_LOCALE));
		if (DEFAULT_LOCALE == null) DEFAULT_LOCALE = Locale.US;
		if (files.length > 0) for (File f : files) {
			Locale loc = Translator.parseLocale(f.getName().substring(9, f.getName().length()-5));
			if (loc != null) {
				LANG_BY_LOCALE.put(loc, new Lang(loc, f));
			}
		}
		if (!LANG_BY_LOCALE.containsKey(DEFAULT_LOCALE)) DEFAULT_LOCALE = Locale.US;
		if (!LANG_BY_LOCALE.containsKey(Locale.US)) {
			File f = new File(dir, "messages-en_US.json");
			try {
				if (!f.exists()) f.createNewFile();
				LANG_BY_LOCALE.put(Locale.US, new Lang(Locale.US, f));
			} catch (IOException e) {
				e.printStackTrace();
				LANG_BY_LOCALE.put(Locale.US, new Lang(Locale.US, LangKey.asJsonObject()));
			}
		}
	}
	
	
	/**
	 * Returns Lang instance according to given Locale.
	 * @param locale
	 * @return Lang instance related to this Locale or default if unknown locale.
	 */
	public static @NotNull Lang get(@Nullable Locale locale) {
		return LANG_BY_LOCALE.containsKey(locale)? LANG_BY_LOCALE.get(locale) : LANG_BY_LOCALE.get(DEFAULT_LOCALE);
	}
	/**
	 * Returns Lang instance according to given sender.
	 * @param sender
	 * @return Lang instance related to this sender or default if unknown locale.
	 */
	public static @NotNull Lang get(@Nullable CommonSender sender) {
		if (sender == null) return def();
		if (sender instanceof CommonPlayer) return Lang.get(((CommonPlayer) sender).locale());
		return Lang.get(Locale.getDefault());
	}
	public static @NotNull Locale getValidLocale(@NotNull CommonSender sender) {
		if (sender instanceof CommonPlayer) return LANG_BY_LOCALE.containsKey(((CommonPlayer) sender).locale()) ? ((CommonPlayer) sender).locale() : DEFAULT_LOCALE;
		return Locale.getDefault();
	}
	public static @NotNull Lang def() {
		return LANG_BY_LOCALE.get(DEFAULT_LOCALE);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	private final Map<LangKey, String> texts = new HashMap<>();
	
	private Lang(@NotNull Locale loc, @NotNull File f) {
		JsonObject json = null;
		try (FileReader fr = new FileReader(f)) {
			json = GSON_SERIALIZER.fromJson(fr, JsonObject.class);
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
			return;
		}
		if (json == null) json = new JsonObject();
		if (loadLang(json) > 0) {
			try (FileWriter fw = new FileWriter(f)) {
				GSON_SERIALIZER.toJson(json, fw);
			} catch (JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Lang(@NotNull Locale loc, @NotNull JsonObject json) {
		loadLang(json);
	}
	
	private int loadLang(@NotNull JsonObject json) {
		int modified = 0;
		for (LangKey key : LangKey.values()) {
			if (!key.isModifiable()) continue;
			if (!json.has(key.getPath())) {
				json.addProperty(key.getPath(), key.getDefString());
				modified++;
			}
			texts.put(key, json.get(key.getPath()).getAsString());
		}
		return modified;
	}
	
	public @NotNull String text(@NotNull LangKey key, @NotNull Object... replacements) {
		Validate.notNull(key, "key cannot be null");
		Validate.noNullElements(replacements, "replacement cannot be null");
		String str = this.texts.get(key);
		for (int i = 0; i < replacements.length; i++) {
			str = str.replace("{"+i+"}", String.valueOf(replacements[i]));
		}
		return str;
	}
	
	public @NotNull Component translateComponent(Component comp) {
		if (comp instanceof TranslatableComponent) {
			TranslatableComponent trans = (TranslatableComponent) comp;
			if (isLangTranslatable(trans)) {
				LangKey key = LangKey.getByPath(trans.key().substring(TRANSLATABLE_PREFIX.length()));
				if (key != null) {
					Style style = trans.style();
					final List<Component> args = trans.args();
					comp = Component.text(this.text(key), style).children(comp.children());
					String pattern = String.join("|", IntStream.range(0, args.size()).mapToObj(String::valueOf).toArray(String[]::new));
					final TextReplacementConfig repl = TextReplacementConfig.builder().match("\\{("+pattern+")\\}")
							.replacement((match, builder) -> args.get(Integer.parseInt(match.group(1)))).build();
					comp = comp.replaceText(repl);
				}
			}
		}
		if (comp.hoverEvent() != null && comp.hoverEvent().value() instanceof Component) comp = comp.hoverEvent(translateComponent((Component) comp.hoverEvent().value()));
		return comp.children(comp.children().stream().map(this::translateComponent).collect(Collectors.toList()));
	}
	
	public static boolean isLangTranslatable(TranslatableComponent comp) {
		return comp.key().startsWith(TRANSLATABLE_PREFIX);
	}

}
