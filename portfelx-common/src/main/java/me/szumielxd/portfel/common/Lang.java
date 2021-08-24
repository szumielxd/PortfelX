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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
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
		
		
		
		ERROR_COMMAND_EXECUTION("error.command-execution", "No way! An error occured while attempting to perform this command! See console for any usefull information."),
		ERROR_COMMAND_PERMISSION("error.command-permission", "No! You can't just run this command, you need the access!"),
		ERROR_COMMAND_USER_NOT_LOADED("error.user-not-loaded", "Sorry, but your data is not loaded. Are you a ghost?!"),
		ERROR_COMMAND_CONSOLE_ONLY("error.command-console-only", "You can do many things, but you must be a console to execute this command!"),
		ERROR_COMMAND_PLAYERS_ONLY("error.command-players-only", "You're just only a console! This command requires something more... a PLAYER..."),
		
		COMMAND_ARGTYPES_USER_DISPLAY("command.arg-types.user.display", "user"),
		COMMAND_ARGTYPES_USER_DESCRIPTION("command.arg-types.user.description", "user representated by name or unique id"),
		COMMAND_ARGTYPES_USER_ERROR("command.arg-types.user.error", "A user for {0} could not be found."),
		//
		COMMAND_ARGTYPES_SERVERNAME_DISPLAY("command.arg-types.servername.display", "serverName"),
		COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION("command.arg-types.servername.description", "user-friendly text representation of server instance (not the same as bungee serverName)"),
		//
		COMMAND_ARGTYPES_HASHKEY_DISPLAY("command.arg-types.hashkey.display", "hashKey"),
		COMMAND_ARGTYPES_HASHKEY_DESCRIPTION("command.arg-types.hashkey.description", "hash provided by subject server used to encode messages (see `server-key.dat` file in client-server's walet directory)"),
		//
		COMMAND_ARGTYPES_SERVER_DISPLAY("command.arg-types.server.display", "server"),
		COMMAND_ARGTYPES_SERVER_DESCRIPTION("command.arg-types.server.description", "server representated by friendly-name or unique id"),
		COMMAND_ARGTYPES_SERVER_ERROR("command.arg-types.server.error", "A server for {0} could not be found."),
		//
		COMMAND_ARGTYPES_ORDER_DISPLAY("command.arg-types.order.display", "order"),
		COMMAND_ARGTYPES_ORDER_DESCRIPTION("command.arg-types.order.description", "name of global order"),
		COMMAND_ARGTYPES_ORDER_ERROR("command.arg-types.order.error", "A order for {0} could not be found. Remember to firstly create it in orders.yml file."),
		//
		COMMAND_ARGTYPES_ECO_AMOUNT_DISPLAY("command.arg-types.eco-amount.display", "amount"),
		COMMAND_ARGTYPES_ECO_AMOUNT_DESCRIPTION("command.arg-types.eco-amount.description", "amount of money"),
		COMMAND_ARGTYPES_ECO_AMOUNT_ERROR("command.arg-types.eco-amount.error", "Hey! {0} is not valid amount. Remember amount is represented by an positive integer."),
		//
		COMMAND_ARGTYPES_REASON_DISPLAY("command.arg-types.reason.display", "reason"),
		COMMAND_ARGTYPES_REASON_DESCRIPTION("command.arg-types.reason.description", "reason of this action"),
		COMMAND_ARGTYPES_REASON_ERROR("command.arg-types.reason.error", "You must provide a good reason to run this command."),
		//
		COMMAND_ARGTYPES_INTOP_DISPLAY("command.arg-types.in-top.display", "reason"),
		COMMAND_ARGTYPES_INTOP_DESCRIPTION("command.arg-types.in-top.description", "reason of this action"),
		COMMAND_ARGTYPES_INTOP_ERROR("command.arg-types.in-top.error", "Did you know {0} is not true nor false?"),
		//
		COMMAND_ARGTYPES_LOGPAGE_DISPLAY("command.arg-types.log-page.display", "page"),
		COMMAND_ARGTYPES_LOGPAGE_DESCRIPTION("command.arg-types.log-page.description", "current page of logs"),
		//
		COMMAND_ARGTYPES_LOGSIZE_DISPLAY("command.arg-types.log-size.display", "size"),
		COMMAND_ARGTYPES_LOGSIZE_DESCRIPTION("command.arg-types.log-size.description", "size of one page"),
		COMMAND_ARGTYPES_LOGSIZE_ERROR("command.arg-types.log-size.error", "Are you stupid or stupid? Page size of {0} is definitly unsafe. Max allowed is {1}."),
		//
		COMMAND_ARGTYPES_LOGTARGET_DISPLAY("command.arg-types.log-target.display", "target"),
		COMMAND_ARGTYPES_LOGTARGET_DESCRIPTION("command.arg-types.log-target.description", "string representation of action's target"),
		//
		COMMAND_ARGTYPES_LOGEXECUTOR_DISPLAY("command.arg-types.log-executor.display", "executor"),
		COMMAND_ARGTYPES_LOGEXECUTOR_DESCRIPTION("command.arg-types.log-executor.description", "string representation of action's executor"),
		//
		COMMAND_ARGTYPES_LOGSERVER_DISPLAY("command.arg-types.log-executor.display", "server"),
		COMMAND_ARGTYPES_LOGSERVER_DESCRIPTION("command.arg-types.log-executor.description", "string representation of server where action has place"),
		//
		COMMAND_ARGTYPES_LOGORDER_DISPLAY("command.arg-types.log-executor.display", "order"),
		COMMAND_ARGTYPES_LOGORDER_DESCRIPTION("command.arg-types.log-executor.description", "string representation of order's name"),
		//
		COMMAND_ARGTYPES_LOGACTION_DISPLAY("command.arg-types.log-executor.display", "action"),
		COMMAND_ARGTYPES_LOGACTION_DESCRIPTION("command.arg-types.log-executor.description", "string representation of action's type"),
		//
		COMMAND_ARGTYPES_LOGVALCOND_DISPLAY("command.arg-types.log-value-condition.display", "condition"),
		COMMAND_ARGTYPES_LOGVALCOND_DESCRIPTION("command.arg-types.log-value-condition.description", "conditions describing range of values"),
		//
		COMMAND_ARGTYPES_LOGBALCOND_DISPLAY("command.arg-types.log-balance-condition.display", "condition"),
		COMMAND_ARGTYPES_LOGBALCOND_DESCRIPTION("command.arg-types.log-balance-condition.description", "conditions describing range of balances"),
		
		
		COMMAND_HELP_DESCRIPTION("command.help.decription", "List all available portfel subcommands."),
		//
		COMMAND_SYSTEM_DESCRIPTION("command.system.description", "All portfel setup related commands."),
		//
		COMMAND_SYSTEM_REGISTERSERVER_DESCRIPTION("command.system.registerserver.description", "Register your current server with given friendly name."),
		COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT("command.system.registerserver.timeout", "Are you sure, you provided valid hashKey and server you want to register has up to date version of Portfel? He's not responding..."),
		COMMAND_SYSTEM_REGISTERSERVER_ALREADY("command.system.registerserver.already", "Is there any intelligent reason to register already registered server? Pro Tip: Check ID {0}."),
		COMMAND_SYSTEM_REGISTERSERVER_SUCCESS("command.system.registerserver.success", "You did it! You registered new portfel server with friendly name {0} and ID {1}!"),
		COMMAND_SYSTEM_REGISTERSERVER_ERROR("command.system.registerserver.error", "This... This was very interesting. Server returned an unknown response."),
		COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_NEEDED("command.system.registerserver.servername-needed", "We need a user friendly and memorable text for use as shorthand of server ID. Please provide id."),
		COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY("command.system.registerserver.servername-already", "Did you remember this shorthand is already in use for another server?"),
		//
		COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION("command.system.unregisterserver.description", "Unregister given server."),
		COMMAND_SYSTEM_UNREGISTERSERVER_SUCCESS("command.system.unregisterserver.success", "Successfully unregistered server with friendly name {0} and ID {1}."),
		//
		COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION("command.system.server.grant.description", "Grant selected server access to specified globar order."),
		COMMAND_SYSTEM_SERVER_GRANT_SUCCESS("command.system.server.grant.success", "Successfully granted {0} access to {1} global order."),
		COMMAND_SYSTEM_SERVER_GRANT_ALREADY("command.system.server.grant.already", "The same global order cannot be granted twice for the same server."),
		//
		COMMAND_SYSTEM_SERVER_REVOKE_DESCRIPTION("command.system.server.revoke.description", "Revoke selected server access to specified globar order."),
		COMMAND_SYSTEM_SERVER_REVOKE_SUCCESS("command.system.server.revoke.success", "Successfully revoked {0} access to {1} global order."),
		COMMAND_SYSTEM_SERVER_REVOKE_ALREADY("command.system.server.revoke.already", "To revoke an global order, you must first grant it."),
		//
		COMMAND_USER_DESCRIPTION("command.user.description", "User management main command."),
		//
		COMMAND_USER_INFO_DESCRIPTION("command.user.info.description", "Get extended info about user."),
		COMMAND_USER_INFO_HEADER("command.user.info.header", "User Info: {0}"),
		COMMAND_USER_INFO_UUID("command.user.info.uuid", "UUID: {0}"),
		COMMAND_USER_INFO_UUIDTYPE("command.user.info.uuidtype", "(type: {0})"),
		COMMAND_USER_INFO_STATUS("command.user.info.status", "Status: {0}"),
		COMMAND_USER_INFO_USERDATA("command.user.info.userdata", "Userdata:"),
		COMMAND_USER_INFO_BALANCE("command.user.info.balance", "Balance: {0}"),
		COMMAND_USER_INFO_INTOP("command.user.info.intop", "Can be in Top: {0}"),
		COMMAND_USER_INFO_SUGGEST("command.user.info.suggest", "Click to suggest command on chat"),
		COMMAND_USER_INFO_INSERT("command.user.info.intop", "Click+Shift to insert above text on chat"),
		//
		COMMAND_USER_ECO_DESCRIPTION("command.user.eco.description", "Manage user's economy."),
		//
		COMMAND_USER_ECO_SET_DESCRIPTION("command.user.eco.set.description", "Set user's balance to given amount."),
		COMMAND_USER_ECO_SET_SUCCESS("command.user.eco.set.success", "Set {1} as {0}'s balance."),
		//
		COMMAND_USER_ECO_GIVE_DESCRIPTION("command.user.eco.give.description", "Add given amount to user's balance."),
		COMMAND_USER_ECO_GIVE_SUCCESS("command.user.eco.give.success", "Add {1} to {0}'s balance."),
		//
		COMMAND_USER_ECO_TAKE_DESCRIPTION("command.user.eco.take.description", "Remove given amount from user's balance."),
		COMMAND_USER_ECO_TAKE_SUCCESS("command.user.eco.take.success", "Remove {1} from {0}'s balance."),
		COMMAND_USER_ECO_TAKE_SMALLER("command.user.eco.take.smaller", "User balance cannot be smaller than 0."),
		//
		COMMAND_USER_TOP_DESCRIPTION("command.user.top.description", "Manage user's top position."),
		//
		COMMAND_USER_TOP_INFO_DESCRIPTION("command.user.top.info.description", "Get info about user's top."),
		COMMAND_USER_TOP_INFO_INTOP("command.user.top.info.intop", "{0}'s allowed in Top status:"),
		COMMAND_USER_TOP_INFO_POSITION("command.user.top.info.position", "{0}'s position:"),
		//
		COMMAND_USER_TOP_SET_DESCRIPTION("command.user.top.set.description", "Set wheter this user should by available in top."),
		COMMAND_USER_TOP_SET_SUCCESS("command.user.top.set.success", "Set {0}'s in top visibility to {1}."),
		COMMAND_USER_TOP_SET_ALREADY("command.user.top.set.already", "{0}'s in top visibility is already set to {1}."),
		//
		COMMAND_LOG_DESCRIPTION("command.log.description", "Log management main command."),
		//
		COMMAND_LOG_READ_DESCRIPTION("command.log.read.description", "Read logs."),
		COMMAND_LOG_READ_HEADER("command.log.read.header", "Showing last activities"),
		COMMAND_LOG_READ_PAGE("command.log.read.page", "page {0} of {1}"),
		COMMAND_LOG_READ_TIME_AGO("command.log.read.time-ago", "{0} ago"),
		
		
		COMMAND_USAGE_TITLE("command.usage.title", "Command Usage - {0}"),
		COMMAND_USAGE_ALIASES("command.usage.aliases", "Aliases"),
		COMMAND_USAGE_ARGUMENTS("command.usage.arguments", "Arguments"),
		
		COMMAND_SUBCOMMANDS_TITLE("command.subcommands.title", "{0} Sub Commands:"),
		COMMAND_SUBCOMMANDS_EXECUTE("command.subcommands.execution", "Click to execute this command"),
		COMMAND_SUBCOMMANDS_INSERT("command.subcommands.insertion", "Click+Shift to insert this command"),
		
		COMMAND_MAIN_RUNNING("command.main.running", "Running {0}."),
		COMMAND_MAIN_USE("command.main.use", "Use {0} to view available commands."),
		
		TOKEN_CHECK_USAGE("token.check.usage", "Correct usage: /{0} <token>"),
		TOKEN_CHECK_ALREADY("token.check.already", "Why are you spamming me? Wait for the result of previus check."),
		TOKEN_CHECK_FULLPOOL("token.check.full-pool", "So many players to check, so few resources to do this. Please wait, the pool is full."),
		TOKEN_CHECK_INVALID("token.check.invalid", "Probably you provided an inexistient token. Prove? It doesn't exist!"),
		TOKEN_CHECK_SERVER_INVALID_REGISTERED("token.check.server.invalid.registered", "This game mode doesn't support tokens. Please try another game mode, or just throw it away..."),
		TOKEN_CHECK_SERVER_INVALID_WHITELIST("token.check.server.invalid.whitelist", "This token doesn't like this game mode. But it should like: {0}"),
		
		SHOP_TITLE("shop.title", "Wallet ({0})"),
		SHOP_ORDER_PRICE("shop.order.price", "Price: {0}"),
		SHOP_ORDER_DESCRIPTION("shop.order.description", "Description:"),
		SHOP_ORDER_PURCHASED("shop.order.purchased", "Purchased"),
		SHOP_ORDER_TERMS("shop.order.terms", "Terms of service:"),
		//
		SHOP_CONFIRM_TITLE("shop.confirm.title", "Are you sure? That's {0}!"),
		SHOP_CONFIRM_YES_TITLE("shop.confirm.yes.title", "Yes"),
		SHOP_CONFIRM_YES_DESCRIPTION("shop.confirm.yes.description", "I'm sure i want to spend {0} for {1}!"),
		SHOP_CONFIRM_NO_TITLE("shop.confirm.no.title", "No"),
		SHOP_CONFIRM_NO_DESCRIPTION("shop.confirm.no.description", "Wait! I must pay for it!?"),
		
		LOG_PREFIX("log.prefix", "LOG"),
		LOG_SUGGEST("log.suggest", "Click to insert displayname on chat"),
		LOG_INSERT("log.insert", "Click+Shift to insert unique ID on chat"),
		LOG_VALUE_ACTION("log.value.action", "Action: {0}"),
		LOG_VALUE_OLD_BALANCE("log.value.old-balance", "Old balance: {0}"),
		LOG_VALUE_DATE("log.value.date", "Date: {0}"),
		
		MAIN_VALUENAME_DESCRIPTION("main.value-name.description", "Description:"),
		MAIN_VALUENAME_ALIASES("main.value-name.aliases", "Aliases:"),
		MAIN_VALUENAME_ENABLED("main.value-name.enabled", "Enabled:"),
		MAIN_VALUENAME_AUTHORS("main.value-name.aliases", "Authors:"),
		MAIN_VALUENAME_PERMISSION("main.value-name.permission", "Permission:"),
		
		MAIN_CURRENCY_FORMAT("main.currrency.format", "${0}"),
		MAIN_VALUE_TRUE("main.value.true", "true"),
		MAIN_VALUE_FALSE("main.value.false", "false"),
		MAIN_VALUE_YES("main.value.yes", "yes"),
		MAIN_VALUE_NO("main.value.no", "no"),
		MAIN_VALUE_ONLINE("main.value.online", "Online"),
		MAIN_VALUE_OFFLINE("main.value.offline", "Offline"),
		MAIN_VALUE_TIME_MINUTES("main.value.time.minutes", "{0}m"),
		MAIN_VALUE_TIME_HOURS("main.value.time.hours", "{0}h"),
		MAIN_VALUE_TIME_DAYS("main.value.time.days", "{0}d"),
		MAIN_VALUE_TIME_YEARS("main.value.time.years", "{0}y"),
		
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
	public static void load(@NotNull File dir, @NotNull Portfel plugin) {
		if (dir.isFile() && dir.exists()) dir.delete();
		if (!dir.exists()) dir.mkdirs();
		File[] files = dir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
		DEFAULT_LOCALE = Translator.parseLocale(plugin.getConfiguration().getString(ConfigKey.LANG_DEFAULT_LOCALE));
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
