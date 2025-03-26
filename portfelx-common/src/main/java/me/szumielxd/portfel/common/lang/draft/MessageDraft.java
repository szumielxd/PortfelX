package me.szumielxd.portfel.common.lang.draft;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import lombok.RequiredArgsConstructor;
import me.szumielxd.legacyminiadventure.LegacyMiniadventure;
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonAudience;
import me.szumielxd.portfel.api.objects.CommonGroupAudience;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.ComponentUtils;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public abstract class MessageDraft {
	
	protected abstract @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion);
	
	public abstract @NotNull String toMinimessageString(@NotNull Lang lang, @NotNull ChatVersion chatVersion);
	
	protected final Component replaceClickAndInsertion(Component comp, Pattern pattern, Function<MatchResult, String> replacer) {
		var click = comp.clickEvent();
		if (click != null) {
			comp = comp.clickEvent(ClickEvent.clickEvent(click.action(), pattern.matcher(click.value()).replaceAll(replacer)));
		}
		if (comp.insertion() != null) {
			comp = comp.insertion(pattern.matcher(comp.insertion()).replaceAll(replacer));
		}
		return comp.children(comp.children().stream()
				.map(c -> replaceClickAndInsertion(c, pattern, replacer))
				.toList());
	}
	
	protected final @NotNull List<Component> toComponentList(@NotNull Lang lang, @NotNull ChatVersion chatVersion, @NotNull Pattern separator) {
		return List.of(ComponentUtils.split(toComponent(lang, chatVersion), separator));
	}
	
	protected final @NotNull List<Component> toComponentList(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return List.of(ComponentUtils.split(toComponent(lang, chatVersion), ComponentUtils.NEWLINE_PATTERN));
	}
	
	public final @NotNull MessageDraft append(@NotNull MessageDraft message, boolean appendToLastChild) {
		return new ComboMessageDraft(this, message, appendToLastChild);
	}
	
	public final @NotNull MessageDraft append(@NotNull MessageDraft message) {
		return append(message, false);
	}
	
	public final @NotNull MessageDraft append(@NotNull Object message, boolean appendToLastChild) {
		return new ComboMessageDraft(this, plain(message), appendToLastChild);
	}
	
	public final @NotNull MessageDraft append(@NotNull Object message) {
		return append(message, false);
	}
	
	public final @NotNull MessageDraft parameterized(Object... parameters) {
		return new ParameterizedMessageDraft(this, Stream.of(parameters)
				.map(MessageDraft::plain)
				.toArray(MessageDraft[]::new));
	}
	
	public final @NotNull MessageDraft parameterized(MessageDraft... parameters) {
		return new ParameterizedMessageDraft(this, parameters);
	}
	
	public final @NotNull MessageDraft placeholders(Map<String, MessageDraft> replacements) {
		return new PlaceholdersMessageDraft(this, replacements);
	}
	
	public final @NotNull MessageDraft plainPlaceholders(Map<String, Object> replacements) {
		return placeholders(replacements.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> plain(e.getValue()))));
	}
	
	public final @NotNull MessageDraft deepPlaceholders(@NotNull Pattern pattern, @NotNull Function<String, MessageDraft> replacer) {
		return deepPlaceholders(this, pattern, replacer);
	}
	
	public final @NotNull MessageDraft prefixed() {
		return MainLangKey.PREFIX.draft().append(this);
	}
	
	public @NotNull JsonElement build(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return GsonComponentSerializer.gson().serializeToTree(toComponent(lang, chatVersion));
	}
	
	public <C> @NotNull C buildComponent(@NotNull CommonSender<C> sender) {
		return sender.getPlugin().getComponentMapper().kyori().kyoriToComponent(toComponent(sender));
	}
	
	public <C> @NotNull C buildDefaultComponent(@NotNull Portfel<C> plugin) {
		return plugin.getComponentMapper().kyori().kyoriToComponent(
				toComponent(Lang.def()));
	}
	
	public @NotNull String buildPlain(@NotNull Lang lang) {
		return PlainMessageDraft.SERIALIZER.serialize(toComponent(lang));
	}
	
	public @NotNull String buildLegacy(@NotNull Lang lang) {
		return LegacyMessageDraft.SERIALIZER.serialize(toComponent(lang));
	}
	
	public <C> @NotNull String buildLegacy(@NotNull CommonSender<C> sender) {
		return LegacyMessageDraft.SERIALIZER.serialize(toComponent(sender));
	}
	
	public <C> @NotNull List<C> buildComponentList(@NotNull CommonSender<C> sender) {
		return toComponentList(sender).stream()
				.map(sender.getPlugin().getComponentMapper().kyori()::kyoriToComponent)
				.toList();
	}
	
	public @NotNull List<String> buildPlainList(@NotNull Lang lang) {
		return toComponentList(lang).stream()
				.map(PlainMessageDraft.SERIALIZER::serialize)
				.toList();
	}
	
	public @NotNull List<String> buildLegacyList(@NotNull Lang lang) {
		return toComponentList(lang).stream()
				.map(LegacyMessageDraft.SERIALIZER::serialize)
				.toList();
	}
	
	public <C> @NotNull List<String> buildLegacyList(@NotNull CommonSender<C> sender) {
		return toComponentList(sender).stream()
				.map(LegacyMessageDraft.SERIALIZER::serialize)
				.toList();
	}
	
	public <C> void send(@NotNull CommonAudience<C> audience) {
		if (audience instanceof CommonGroupAudience<?>) {
			((CommonGroupAudience<C>) audience).getAudience().forEach(a -> send(a));
		} else if (audience instanceof CommonSender<?>) {
			audience.sendMessage(buildComponent((CommonSender<C>) audience));
		} else {
			audience.sendMessage(buildDefaultComponent(audience.getPlugin()));
		}
	}
	
	public <C> void send(@NotNull CommonSender<C> sender, boolean prefix) {
		if (prefix) {
			sendPrefixed(sender);
		} else {
			send(sender);
		}
	}
	
	public <C> void sendPrefixed(@NotNull CommonSender<C> sender) {
		prefixed().send(sender);
	}
	
	
	private @NotNull Component toComponent(@NotNull Lang lang) {
		return toComponent(lang, ChatVersion.NORMAL);
	}
	
	private @NotNull List<Component> toComponentList(@NotNull Lang lang) {
		return toComponentList(lang, ChatVersion.NORMAL);
	}
	
	private <C> @NotNull Component toComponent(@NotNull CommonSender<C> sender) {
		Optional<Integer> protocolId = getProtocolId(sender);
		return toComponent(Lang.get(sender), ChatVersion.getCorrect(protocolId));
	}
	
	private <C> @NotNull List<Component> toComponentList(@NotNull CommonSender<C> sender) {
		Optional<Integer> protocolId = getProtocolId(sender);
		return toComponentList(Lang.get(sender), ChatVersion.getCorrect(protocolId));
	}
	
	private <C> Optional<Integer> getProtocolId(@NotNull CommonSender<C> sender) {
		return Optional.of(sender)
				.filter(CommonPlayer.class::isInstance)
				.map(CommonPlayer.class::cast)
				.map(CommonPlayer::protocolId);
	}
	
	
	public static @NotNull MessageDraft plain(@Nullable Object obj) {
		if (obj instanceof MessageDraft draft) {
			return draft;
		} else if (obj instanceof LangKey key) {
			return lang(key);
		}
		return new PlainMessageDraft(obj);
	}
	
	public static @NotNull MessageDraft space() {
		return PlainMessageDraft.SPACE;
	}
	
	public static @NotNull MessageDraft newline() {
		return PlainMessageDraft.NEWLINE;
	}
	
	public static @NotNull MessageDraft empty() {
		return PlainMessageDraft.EMPTY;
	}
	
	public static @NotNull MessageDraft trueFalse(boolean val) {
		return val ? MainLangKey.MAIN_VALUE_TRUE.draft()
				: MainLangKey.MAIN_VALUE_FALSE.draft();
	}
	
	public static @NotNull MessageDraft onlineStatus(boolean online) {
		return online ? MainLangKey.MAIN_VALUE_ONLINE.draft()
				: MainLangKey.MAIN_VALUE_OFFLINE.draft();
	}
	
	public static @NotNull MessageDraft uuidType(@Nullable UUID uuid) {
		return MiscUtils.isOnlineModeUUID(uuid) ? MainLangKey.MAIN_VALUE_UUID_ONLINE.draft()
				: MainLangKey.MAIN_VALUE_UUID_OFFLINE.draft();
	}
	
	public static @NotNull MessageDraftArray array(@NotNull MessageDraft... elements) {
		return new MessageDraftArray(elements);
	}
	
	public static @NotNull MessageDraftArray array(@NotNull Object... elements) {
		return new MessageDraftArray(Stream.of(elements)
				.map(MessageDraft::plain)
				.toArray(MessageDraft[]::new));
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join() {
		return join((MessageDraft) null);
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join(@Nullable Object separator) {
		return join(plain(separator));
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join(@Nullable MessageDraft separator) {
		return Collector.of(() -> new MessageDraftJoiner(separator),
				MessageDraftJoiner::append,
				(a, b) -> a.append(b.build()),
				MessageDraftJoiner::build);
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraftArray> joinFlattened() {
		return joinFlattened((MessageDraft) null);
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraftArray> joinFlattened(@Nullable Object separator) {
		return joinFlattened(plain(separator));
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraftArray> joinFlattened(@Nullable MessageDraft separator) {
		return Collector.of(() -> new MessageDraftArrayJoiner(separator),
				MessageDraftArrayJoiner::append,
				(a, b) -> a.append(b.build()),
				MessageDraftArrayJoiner::build);
	}
	
	public static @NotNull LangMessageDraft lang(@NotNull LangKey key) {
		return new LangMessageDraft(key);
	}
	
	public static @NotNull MiniMessageDraft minimessage(@NotNull String text) {
		return new MiniMessageDraft(text);
	}
	
	public static @NotNull LegacyMessageDraft legacy(@NotNull String text) {
		return new LegacyMessageDraft(text);
	}
	
	public static @NotNull String stripMiniTags(@NotNull String text) {
		return LegacyMiniadventure.get().stripTags(text);
	}
	
	public static @NotNull DeepPlaceholdersMessageDraft deepPlaceholders(@NotNull String text, @NotNull Pattern pattern, @NotNull Function<String, MessageDraft> replacer) {
		return deepPlaceholders(minimessage(text), pattern, replacer);
	}
	
	public static @NotNull DeepPlaceholdersMessageDraft deepPlaceholders(@NotNull MessageDraft draft, @NotNull Pattern pattern, @NotNull Function<String, MessageDraft> replacer) {
		return new DeepPlaceholdersMessageDraft(draft, pattern, replacer);
	}
	
	
	@RequiredArgsConstructor
	private static class MessageDraftJoiner {
		
		private final @Nullable MessageDraft separator;
		private @Nullable MessageDraft base;
		
		public @NotNull MessageDraftJoiner append(@NotNull MessageDraft draft) {
			if (base == null) {
				base = draft;
			} else {
				if (separator != null) {
					base = base.append(separator);
				}
				base = base.append(draft);
			}
			return this;
		}
		
		public @NotNull MessageDraft build() {
			return base == null ? plain("") : base;
		}
		
	}
	
	
	@RequiredArgsConstructor
	private static class MessageDraftArrayJoiner {
		
		private final @Nullable MessageDraft separator;
		private final @NotNull List<MessageDraft> elements = new LinkedList<>();
		
		public @NotNull MessageDraftArrayJoiner append(@NotNull MessageDraft draft) {
			elements.add(draft);
			return this;
		}
		
		public @NotNull MessageDraftArray build() {
			if (separator != null) {
				var iter = elements.listIterator(Math.min(1, elements.size()));
				while (iter.hasNext()) {
					iter.add(separator);
					iter.next();
				}
			}
			return new MessageDraftArray(elements.toArray(MessageDraft[]::new));
		}
		
	}

}
