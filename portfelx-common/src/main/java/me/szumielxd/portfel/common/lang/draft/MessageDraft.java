package me.szumielxd.portfel.common.lang.draft;

import java.util.Optional;
import java.util.stream.Collector;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public abstract class MessageDraft {
	
	protected abstract @NotNull Component toComponent(@NotNull Lang lang, @NotNull ChatVersion chatVersion);
	
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
	
	public @NotNull JsonElement build(@NotNull Lang lang, @NotNull ChatVersion chatVersion) {
		return GsonComponentSerializer.gson().serializeToTree(toComponent(lang, chatVersion));
	}
	
	public @NotNull String buildPlain(@NotNull Lang lang) {
		return PlainTextComponentSerializer.plainText().serialize(toComponent(lang, ChatVersion.NORMAL));
	}
	
	public <C> void send(CommonSender<C> sender) {
		send(sender, false);
	}
	
	public <C> void send(CommonSender<C> sender, boolean prefix) {
		MessageDraft base = prefix ? MainLangKey.PREFIX.draft().append(this) : this;
		Optional<Integer> protocolId = Optional.of(sender)
				.filter(CommonPlayer.class::isInstance)
				.map(CommonPlayer.class::cast)
				.map(CommonPlayer::protocolId);
		sender.sendMessage(sender.getPlugin().getComponentMapper().kyori().kyoriToComponent(
				base.toComponent(Lang.get(sender), ChatVersion.getCorrect(protocolId))));
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
		return plain(" ");
	}
	
	public static @NotNull MessageDraft newline() {
		return plain("\n");
	}
	
	public static @NotNull MessageDraft array(@NotNull MessageDraft... elements) {
		return new MessageDraftArray(elements);
	}
	
	public static @NotNull MessageDraft array(@NotNull Object... elements) {
		return new MessageDraftArray(Stream.of(elements)
				.map(MessageDraft::plain)
				.toArray(MessageDraft[]::new));
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join() {
		return join((MessageDraft) null);
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join(@Nullable MessageDraft separator) {
		return Collector.of(() -> new MessageDraftJoiner(separator),
				MessageDraftJoiner::append,
				(a, b) -> a.append(b.build()),
				MessageDraftJoiner::build);
	}
	
	public static @NotNull Collector<MessageDraft, ?, MessageDraft> join(@Nullable Object separator) {
		return Collector.of(() -> new MessageDraftJoiner(plain(separator)),
				MessageDraftJoiner::append,
				(a, b) -> a.append(b.build()),
				MessageDraftJoiner::build);
	}
	
	public static @NotNull MessageDraft lang(@NotNull LangKey key) {
		return new LangMessageDraft(key);
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

}
