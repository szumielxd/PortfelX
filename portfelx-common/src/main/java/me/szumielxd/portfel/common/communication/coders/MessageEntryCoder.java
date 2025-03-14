package me.szumielxd.portfel.common.communication.coders;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class MessageEntryCoder<T> {
	
	protected final Type targetClass;

	protected static final MessageEntryCoderCreator<Boolean> BOOLEAN = new MessageEntryCoderCreator<>(ByteArrayDataInput::readBoolean, ByteArrayDataOutput::writeBoolean, List.of(Boolean.class, boolean.class)::contains);
	protected static final MessageEntryCoderCreator<Integer> INTEGER = new MessageEntryCoderCreator<>(ByteArrayDataInput::readInt, ByteArrayDataOutput::writeInt, List.of(Integer.class, int.class, Long.class, long.class, Number.class)::contains);
	protected static final MessageEntryCoderCreator<Long> LONG = new MessageEntryCoderCreator<>(ByteArrayDataInput::readLong, ByteArrayDataOutput::writeLong, List.of(Long.class, long.class, Number.class)::contains);
	protected static final MessageEntryCoderCreator<String> UTF = new MessageEntryCoderCreator<>(ByteArrayDataInput::readUTF, ByteArrayDataOutput::writeUTF, cl -> String.class.equals(cl));
	protected static final MessageEntryCoderCreator<String> ASCII = new MessageEntryCoderCreator<>(
			in -> IntStream.range(0, in.readByte() + 128)
					.mapToObj(j -> in.readByte())
					.map(MessageEntryCoder::byteToAscii)
					.filter(ch -> !ch.equals(' '))
					.map(String::valueOf)
					.collect(Collectors.joining()),
			(out, data) -> Stream.concat(
					Stream.of((byte) (data.length() - 128)),
					IntStream.range(0, data.length())
							.mapToObj(data::charAt)
							.map(MessageEntryCoder::asciiToByte))
					.forEach(out::write),
			String.class::equals);
	protected static final MessageEntryCoderCreator<UUID> UUID = new MessageEntryCoderCreator<>(
			in -> new UUID(in.readLong(), in.readLong()),
			(out, data) -> {
				out.writeLong(data.getMostSignificantBits());
				out.writeLong(data.getLeastSignificantBits());
			},
			UUID.class::equals);
	protected static final MessageEntryCoderCreator<Object> OBJECT_FETCHER = new MessageEntryCoderCreator<>(
			MessageEntryCoder::decodeObject,
			MessageEntryCoder::encodeObject,
			MessageEntryCoder::validateObject);
	protected static final MessageEntryCoderCreator<Enum<?>> ENUM = new MessageEntryCoderCreator<>(
			MessageEntryCoder::decodeEnum,
			MessageEntryCoder::encodeEnum,
			type -> type instanceof Class<?> clazz && clazz.isEnum());
	protected static final MessageEntryCoderCreator<EncryptedObject<?>> CRYPTO = new MessageEntryCoderCreator<>(
			MessageEntryCoder::decodeCrypto,
			MessageEntryCoder::encodeCrypto,
			MessageEntryCoder::validateCrypto);
	
	
	public abstract @NotNull T decode(@NotNull ByteArrayDataInput in);
	
	public abstract void encode(@NotNull ByteArrayDataOutput out, @NotNull T data);
	
	private static char byteToAscii(byte b) {
		return (char)(b + 128);
	}
	
	private static byte asciiToByte(char b) {
		return (byte)(b - 128);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<?>> T decodeEnum(@NotNull ByteArrayDataInput in, Type type) {
		Class<T> clazz = (Class<T>) type;
		try {
			String name = ASCII.decoder.apply(in, String.class).toUpperCase();
			for (var e : clazz.getEnumConstants()) {
				if (name.equals(e.name().toUpperCase())) {
					return e;
				}
			}
			throw new IllegalArgumentException("Invalid enum entry name: `%s`".formatted(name));
		} catch (IllegalArgumentException | SecurityException e) {
			throw new RuntimeException("Could not decode enum for class `%s`:".formatted(clazz), e);
		}
	}
	
	private static <T extends Enum<?>> void encodeEnum(@NotNull ByteArrayDataOutput out, T e) {
		ASCII.encoder.accept(out, e.name());
	}
	
	private static boolean validateCrypto(@NotNull Type type) {
		return type instanceof ParameterizedType param
				&& param.getRawType() instanceof Class<?> clazz
				&& EncryptedObject.class.isAssignableFrom(clazz);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> EncryptedObject<T> decodeCrypto(@NotNull ByteArrayDataInput in, Type type) {
		var paramType = (ParameterizedType) type;
		var genericType = (Class<T>) paramType.getActualTypeArguments()[0];
		return EncryptedObject.readFromBytes(in, genericType);
	}
	
	private static <T> void encodeCrypto(@NotNull ByteArrayDataOutput out, EncryptedObject<T> e) {
		e.writeBytes(out);
	}
	
	private static boolean validateObject(@NotNull Type type) {
		try {
			return type instanceof Class<?> clazz
					&& Stream.of(clazz.getDeclaredFields())
							.map(MessageEntryCoder::checkIfApplicable)
							.allMatch(Optional::isPresent);
		} catch (SecurityException e1) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T decodeObject(@NotNull ByteArrayDataInput in, Type type) {
		Class<? extends T> clazz = (Class<? extends T>) type;
		try {
			T obj = clazz.getDeclaredConstructor().newInstance();
			for (var field : clazz.getDeclaredFields()) {
				var entryMeta = checkIfApplicable(field);
				if (entryMeta.isPresent()) {
					field.setAccessible(true);
					field.set(obj, readFieldValue(in, entryMeta.get(), field.getGenericType()));
					
				}
			}
			return obj;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Could not decode object for class `%s`:".formatted(clazz), e);
		}
	}
	
	private static void encodeObject(@NotNull ByteArrayDataOutput out, Object data) {
		for (var field : data.getClass().getDeclaredFields()) {
			var entryMeta = checkIfApplicable(field);
			if (entryMeta.isPresent()) {
				try {
					field.setAccessible(true);
					writeFieldValue(out, entryMeta.get(), field.get(data));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Could not encode object `%s`:".formatted(data), e);
				}
			}
		}
	}
	
	private static Object readFieldValue(@NotNull ByteArrayDataInput in, @NotNull MessageEntryCoder<?> coder, Type type) {
		if (type instanceof Class<?> clazz && clazz.isArray()) {
			int size = in.readInt();
			Object arr = Array.newInstance(clazz.componentType(), size);
			for (int i = 0; i < size; i++) {
				Array.set(arr, i, readFieldValue(in, coder, clazz.componentType()));
			}
			return arr;
		} else {
			return coder.decode(in);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void writeFieldValue(@NotNull ByteArrayDataOutput out, @NotNull MessageEntryCoder<?> coder, Object data) {
		if (data.getClass().isArray()) {
			int size = Array.getLength(data);
			out.writeInt(size);
			for (int i = 0; i < size; i++) {
				writeFieldValue(out, coder, Array.get(data, i));
			}
		} else {
			((MessageEntryCoder<Object>) coder).encode(out, data);
		}
	}
	
	private static @NotNull Optional<? extends MessageEntryCoder<?>> checkIfApplicable(@NotNull Field field) {
		var entryMeta = field.getAnnotation(MessageEntry.class);
		if (entryMeta != null) {
			var entryType = field.getGenericType();
			entryType = unwrapArrayType(entryType);
			if (!entryMeta.value().isApplicable(entryType)) {
				throw new IllegalArgumentException("Cannot apply `%s` to field of type `%s`".formatted(entryMeta.value().name(), field.getType()));
			}
			return entryMeta.value().getIfValid(entryType);
		}
		return Optional.empty();
	}
	
	private static @NotNull Type unwrapArrayType(@NotNull Type type) {
		while (type instanceof Class<?> clazz && clazz.isArray()) {
			type = clazz.getComponentType();
		}
		return type;
	}
	
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	protected static class MessageEntryCoderCreator<T> {
		
		private final @NotNull BiFunction<@NotNull ByteArrayDataInput, Type, @NotNull T> decoder;
		private final @NotNull BiConsumer<@NotNull ByteArrayDataOutput, @NotNull T> encoder;
		private final @NotNull Predicate<Type> typeValidator;
		
		private MessageEntryCoderCreator(@NotNull Function<@NotNull ByteArrayDataInput, @NotNull T> decoder, @NotNull BiConsumer<@NotNull ByteArrayDataOutput, @NotNull T> encoder, @NotNull Predicate<Type> typeValidator) {
			this((in, clazz) -> decoder.apply(in), encoder, typeValidator);
		}
		
		public boolean isApplicable(@NotNull Type clazz) {
			return this.typeValidator.test(clazz);
		}
		
		public @NotNull Optional<MessageEntryCoder<T>> generateIfValid(Type type) {
			if (isApplicable(type)) {
				return Optional.of(new MessageEntryCoder<T>(type) {
					@Override
					public @NotNull T decode(@NotNull ByteArrayDataInput in) {
						return decoder.apply(in, type);
					}

					@Override
					public void encode(@NotNull ByteArrayDataOutput out, @NotNull T data) {
						encoder.accept(out, data);
					}
				});
			}
			return Optional.empty();
		}
		
	}
	
}
