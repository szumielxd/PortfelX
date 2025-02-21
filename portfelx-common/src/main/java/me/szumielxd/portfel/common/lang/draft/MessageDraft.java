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
import me.szumielxd.legacyminiadventure.VersionableObject.ChatVersion;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public abstract class MessageDraft {
	
	protected abstract @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion);
	
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
	
	public final @NotNull MessageDraft prefixed() {
		return MainLangKey.PREFIX.draft().append(this);
	}
	
	public @NotNull JsonElement build(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return GsonComponentSerializer.gson().serializeToTree(toComponent(lang, chatVersion));
	}
	
	public <C> @NotNull C buildComponent(@NotNull CommonSender<C> sender) {
		Optional<Integer> protocolId = Optional.of(sender)
				.filter(CommonPlayer.class::isInstance)
				.map(CommonPlayer.class::cast)
				.map(CommonPlayer::protocolId);
		return sender.getPlugin().getComponentMapper().kyori().kyoriToComponent(
				toComponent(Lang.get(sender), ChatVersion.getCorrect(protocolId)));
	}
	
	public @NotNull String buildPlain(@NotNull Lang lang) {
		return PlainTextComponentSerializer.plainText().serialize(toComponent(lang, ChatVersion.NORMAL));
	}
	
	public <C> void send(@NotNull CommonSender<C> sender) {
		send(sender, false);
	}
	
	public <C> void send(@NotNull CommonSender<C> sender, boolean prefix) {
		if (prefix) {
			sendPrefixed(sender);
		} else {
			send(sender);
		}
	}
	
	public <C> void sendPrefixed(@NotNull CommonSender<C> sender) {
		this.prefixed().send(sender);
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
