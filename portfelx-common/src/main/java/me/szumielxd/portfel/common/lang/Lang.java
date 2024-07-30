package me.szumielxd.portfel.common.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.configuration.ConfigKey;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import net.kyori.adventure.translation.Translator;

public class Lang {
	
	
	public interface LangKey {
		
		/**
		 * Get the String path.
		 * 
		 * @return The String patch.
		 */
		public String getPath();
		
		public String getDefString();
		
		public boolean isModifiable();
		
		public default MessageDraft draft() {
			return MessageDraft.lang(this);
		}
		
		public default MessageDraft draft(Object... parameters) {
			return draft().parameterized(parameters);
		}
		
		public static @Nullable LangKey getByPath(@Nullable String path) {
			return KEYS_BY_PATH.get(path);
		}
		
		public static LangKey[] values() {
			return KEYS_BY_PATH.values().toArray(new LangKey[KEYS_BY_PATH.size()]);
		}
		
		public static void register(@NotNull Class<? extends Enum<? extends LangKey>> langKeyClass) {
			Stream.of(langKeyClass.getEnumConstants())
					.map(LangKey.class::cast)
					.forEach(key -> Optional.ofNullable(KEYS_BY_PATH.putIfAbsent(key.getPath().toLowerCase(), key)).ifPresent(old -> {
							throw new IllegalArgumentException("Cannot register `%s`. LangKey with path `%s` is already registered for `%s`".formatted(
									langKeyClass.getName() + "#" + key.toString(),
									key.getPath(),
									old.getClass().getName() + "#" + old.toString()));
					}));
		}
		
		public static JsonObject asJsonObject() {
			JsonObject json = new JsonObject();
			for (LangKey key : LangKey.values()) {
				if (key.isModifiable()) json.addProperty(key.getPath(), key.getDefString().replace('§', '&').replace("\n", "\\n"));
			}
			return json;
		}
		
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final Map<String, LangKey> KEYS_BY_PATH = new HashMap<>();
	
	private static Map<Locale, Lang> langByLocale = new HashMap<>();
	private static Locale defaultLocale;
	private static final Pattern FILE_PATTERN = Pattern.compile("messages-[a-z]{2}(_[A-Z]{2})?\\.json");
	private static final Gson GSON_SERIALIZER = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	public static final String TRANSLATABLE_PREFIX = "portfel.";
	
	
	/**
	 * Setup Lang instances. Internal use only
	 * @param locales directory
	 */
	public static void load(@NotNull Path dir, @NotNull Portfel<?> plugin) {
		langByLocale.clear();
		try {
			if (Files.exists(dir) && !Files.isDirectory(dir)) {
				Files.delete(dir);
			}
			if (!Files.exists(dir)) {
				Files.createDirectories(dir);
			}
			defaultLocale = Optional.ofNullable(Translator.parseLocale(plugin.getConfiguration().getString(ConfigKey.LANG_DEFAULT_LOCALE)))
					.orElse(Locale.US);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try (Stream<Path> files = Files.find(dir, 0, (path, attr) -> FILE_PATTERN.matcher(path.getFileName().toString()).matches())) {
			files.forEach(f -> Optional.ofNullable(Translator.parseLocale(f.getFileName().toString().transform(s -> s.substring(9, s.length()-5))))
					.map(loc -> new Lang(loc, f))
					.ifPresent(lang -> langByLocale.put(lang.getLocale(), lang)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!langByLocale.containsKey(defaultLocale)) { // set default locale to US if currently set doesn't exist
			defaultLocale = Locale.US;
		}
		if (!langByLocale.containsKey(Locale.US)) { // load default messages
			Path f = dir.resolve("messages-en_US.json");
			try {
				if (!Files.exists(f) || Files.isDirectory(f)) {
					Files.deleteIfExists(f);
					Files.createFile(f);
				}
				langByLocale.put(Locale.US, new Lang(Locale.US, f));
			} catch (IOException e) {
				e.printStackTrace();
				langByLocale.put(Locale.US, new Lang(Locale.US, LangKey.asJsonObject()));
			}
		}
	}
	
	
	/**
	 * Returns Lang instance according to given Locale.
	 * @param locale
	 * @return Lang instance related to this Locale or default if unknown locale.
	 */
	public static @NotNull Lang get(@Nullable Locale locale) {
		return langByLocale.getOrDefault(locale, def());
	}
	
	/**
	 * Returns Lang instance according to given sender.
	 * @param sender
	 * @return Lang instance related to this sender or default if unknown locale.
	 */
	public static @NotNull Lang get(@Nullable CommonSender<?> sender) {
		if (sender == null) {
			return def();
		}
		if (sender instanceof CommonPlayer<?> player) {
			return Lang.get(player.locale());
		}
		return Lang.get(Locale.getDefault());
	}
	
	public static @NotNull Locale getValidLocale(@NotNull CommonSender<?> sender) {
		if (sender instanceof CommonPlayer<?> player) {
			return langByLocale.containsKey(player.locale()) ? player.locale() : defaultLocale;
		}
		return Locale.getDefault();
	}
	
	public static @NotNull Lang def() {
		return langByLocale.get(defaultLocale);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	private final @Getter Locale locale;
	private final Map<LangKey, String> texts = new HashMap<>();
	
	private Lang(@NotNull Locale locale, @NotNull Path f) {
		this.locale = locale;
		try (BufferedReader fr = Files.newBufferedReader(f)) {
			JsonObject json = Optional.ofNullable(GSON_SERIALIZER.fromJson(fr, JsonObject.class))
					.orElseGet(JsonObject::new);
			if (loadLang(json) > 0) {
				saveToFile(json, f);
			}
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private Lang(@NotNull Locale locale, @NotNull JsonObject json) {
		this.locale = locale;
		loadLang(json);
	}
	
	private void saveToFile(@NotNull JsonObject json, @NotNull Path f) {
		try (BufferedWriter fw = Files.newBufferedWriter(f)) {
			GSON_SERIALIZER.toJson(json, fw);
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private int loadLang(@NotNull JsonObject json) {
		int modified = 0;
		for (LangKey key : LangKey.values()) {
			if (key.isModifiable()) {
				if (!json.has(key.getPath())) {
					json.addProperty(key.getPath(), key.getDefString());
					modified++;
				}
				texts.put(key, json.get(key.getPath()).getAsString());
			}
		}
		return modified;
	}
	
	/*public @NotNull String text(@NotNull LangKey key, @NotNull Object... replacements) {
		String str = this.texts.get(key);
		for (int i = 0; i < replacements.length; i++) {
			str = str.replace("{"+i+"}", String.valueOf(replacements[i]));
		}
		return str;
	}*/
	
	public @NotNull String getValue(@NotNull LangKey key) {
		return this.texts.get(key);
	}
	
	/*public @NotNull Component translateComponent(Component comp) {
		if (comp instanceof TranslatableComponent trans) {
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
	}*/

}
