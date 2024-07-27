package me.szumielxd.portfel.common.communication.coders;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;

public class PacketCoder {
	
	
	public void decode(@NotNull ByteArrayDataInput in, @NotNull MessagePacket message) {
		IdentifiedMessage msgMeta = message.getClass().getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		
	}
	
	protected static void decodee(@NotNull ByteArrayDataInput in, @NotNull MessagePacket message) {
		
	}
	
	
	private static @Nullable MessageEntry checkIfApplicable(@NotNull Field field) {
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
