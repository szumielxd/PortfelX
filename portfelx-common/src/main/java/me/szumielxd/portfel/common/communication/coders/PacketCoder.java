package me.szumielxd.portfel.common.communication.coders;

import java.lang.reflect.Field;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.experimental.UtilityClass;


@UtilityClass
public class PacketCoder {
	
	
	@SuppressWarnings("unchecked")
	public <T extends MessagePacket> Optional<T> decode(@NotNull ByteArrayDataInput in, @NotNull Class<T> messageClass) {
		IdentifiedMessage msgMeta = messageClass.getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		return MessageEntryCoder.OBJECT_FETCHER.generateIfValid(messageClass)
				.map(c -> (T) c.decode(in));
	}
	
	public void encode(@NotNull ByteArrayDataOutput out, @NotNull MessagePacket message) {
		IdentifiedMessage msgMeta = message.getClass().getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		MessageEntryCoder.OBJECT_FETCHER.generateIfValid(message.getClass())
				.ifPresent(c -> c.encode(out, message));
	}
	
	public byte[] encodePacket(@NotNull MessagePacket message) {
		IdentifiedMessage msgMeta = message.getClass().getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(msgMeta.value());
		encode(out, message);
		return out.toByteArray();
	}
	
	
	private @Nullable MessageEntry checkIfApplicable(@NotNull Field field) {
		var entryMeta = field.getAnnotation(MessageEntry.class);
		if (entryMeta != null) {
			var entryType = field.getType();
			if (entryType.isArray()) {
				entryType = entryType.arrayType();
			}
			if (!entryMeta.value().isApplicable(entryType)) {
				throw new IllegalArgumentException("Cannot apply `%s` to field of type `%s`".formatted(entryMeta.value().name(), field.getType()));
			}
		}
		return entryMeta;
	}
	

}
