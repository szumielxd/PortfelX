package me.szumielxd.portfel.common.communication.coders;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.common.utils.CryptoUtils;

@RequiredArgsConstructor
public class EncryptedObject<T> {
	
	
	private final Class<T> type;
	private final byte[] data;
	
	
	public EncryptedObject(Class<T> type, T obj, String key) {
		var out = ByteStreams.newDataOutput();
		MessageEntryCoder.OBJECT_FETCHER.generateIfValid(type)
				.ifPresent(c -> c.encode(out, obj));
		this.type = type;
		this.data = CryptoUtils.encode(out.toByteArray(), key);
	}
	
	
	@SuppressWarnings("unchecked")
	public T decrypt(String key) {
		var in = ByteStreams.newDataInput(CryptoUtils.decode(data, key));
		return MessageEntryCoder.OBJECT_FETCHER.generateIfValid(type)
				.map(c -> (T) c.decode(in))
				.orElseThrow(() -> new IllegalStateException("Cannot find valid decoder for type `%s`".formatted(type)));
	}
	
	
	public void writeBytes(@NotNull ByteArrayDataOutput out) {
		out.writeShort(data.length);
		out.write(data);
	}
	
	
	public static <U> EncryptedObject<U> readFromBytes(@NotNull ByteArrayDataInput in, Class<U> type) {
		byte[] bytes = new byte[in.readShort()];
		in.readFully(bytes);
		return new EncryptedObject<>(type, bytes);
	}
	

}
