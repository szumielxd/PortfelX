package me.szumielxd.portfel.common.communication.coders;

import java.lang.reflect.Field;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class PacketCoder {
	
	
	public PacketCoder() {
		/*try {
			Map<String, Class<? extends MessagePacket>> messages = new HashMap<>();
			ClassLoader loader = getClass().getClassLoader();
			ClassPath.from(loader).getTopLevelClasses(getClass().getPackageName() + ".messages")
					.stream()
					.map(ClassInfo::load)
					.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
					.filter(MessagePacket.class::isAssignableFrom)
					.filter(clazz -> clazz.isAnnotationPresent(IdentifiedMessage.class))
					.forEach(clazz -> {
							IdentifiedMessage msg = clazz.getAnnotation(IdentifiedMessage.class);
							if (messages.containsKey(msg.value())) {
								throw new IllegalStateException("Cannot register packet message %s, because subchannel with name `%s` is already used".formatted(clazz, msg.value()));
							}
							messages.put(msg.value(), clazz.asSubclass(MessagePacket.class));
					});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T extends MessagePacket> Optional<T> decode(@NotNull ByteArrayDataInput in, @NotNull Class<T> messageClass) {
		IdentifiedMessage msgMeta = messageClass.getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		return MessageEntryCoder.OBJECT_FETCHER.generateIfValid(messageClass)
				.map(c -> (T) c.decode(in));
	}
	
	public static void encode(@NotNull ByteArrayDataOutput out, @NotNull MessagePacket message) {
		IdentifiedMessage msgMeta = message.getClass().getAnnotation(IdentifiedMessage.class);
		if (msgMeta == null) {
			throw new IllegalArgumentException("Cannot find @MessageIdentifier annotation within given `message` class");
		}
		MessageEntryCoder.OBJECT_FETCHER.generateIfValid(message.getClass())
				.ifPresent(c -> c.encode(out, message));
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
