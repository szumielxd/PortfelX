package me.szumielxd.portfel.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import me.szumielxd.portfel.api.PortfelProvider;

public class CryptoUtils {
	
	
	private static final byte[] CONTROL_BYTES = new byte[] {0x00, 0x00, 0x00, 0x00};
	private static final int PASSES_COUNT = 3;
	
	
	/**
	 * Encode an byte array.
	 * 
	 * @param bytes array to encode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return encoded array
	 */
	public static byte[] encode(byte[] bytes, String key, int passes) {
		bytes = bytes.clone();
		byte[] encoder = key.getBytes(StandardCharsets.US_ASCII);
		encoder = Arrays.copyOf(encoder, encoder.length+1);
		int byteSize = Byte.MIN_VALUE * -2;
		int index = 0;
		for (int pass = 0; pass < passes; pass++) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) ((bytes[i]+(encoder[(index+i)%encoder.length]))%byteSize);
			}
			index += bytes.length;
		}
		return bytes;
	}
	
	/**
	 * Encode an byte array with default amount of 3 passes.
	 * 
	 * @param bytes array to encode
	 * @param key key used for encoding
	 * @return encoded array
	 */
	public static byte[] encode(byte[] bytes, String key) {
		return encode(bytes, key, PASSES_COUNT);
	}
	
	/**
	 * Decode an byte array.
	 * 
	 * @param bytes array to decode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return decoded array
	 */
	public static byte[] decode(byte[] bytes, String key, int passes) {
		bytes = bytes.clone();
		byte[] encoder = key.getBytes(StandardCharsets.US_ASCII);
		encoder = Arrays.copyOf(encoder, encoder.length+1);
		int byteSize = Byte.MIN_VALUE * -2;
		int index = 0;
		for (int pass = 0; pass < passes; pass++) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) ((bytes[i]-(encoder[(index+i)%encoder.length]))%byteSize);
			}
			index += bytes.length;
		}
		return bytes;
	}
	
	/**
	 * Decode an byte array with default amount of 3 passes.
	 * 
	 * @param bytes array to decode
	 * @param key key used for encoding
	 * @return decoded array
	 */
	public static byte[] decode(byte[] bytes, String key) {
		return decode(bytes, key, PASSES_COUNT);
	}
	
	/**
	 * Append control bytes to start of the array and encode new creation.
	 * 
	 * @param bytes bytes to full encode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return ready to send encoded array
	 */
	public static byte[] fullEncode(byte[] bytes, String key, int passes) {
		byte[] arr = Arrays.copyOf(CONTROL_BYTES, bytes.length+CONTROL_BYTES.length);
		for (int i = 0; i < bytes.length; i++) {
			arr[i+CONTROL_BYTES.length] = bytes[i];
		}
		bytes = encode(arr, key, passes);
		return bytes;
	}
	
	/**
	 * Append control bytes to start of the array and encode new creation with default amount of 3 passes.
	 * 
	 * @param bytes bytes to full encode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return ready to send encoded array
	 */
	public static byte[] fullEncode(byte[] bytes, String key) {
		return fullEncode(bytes, key, PASSES_COUNT);
	}
	
	/**
	 * Decode given array, check validity and get properly trimmed data.
	 * 
	 * @param bytes bytes to full decode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return ready to read decoded array
	 * @throws IllegalArgumentException when the array cannot be properly decoded using given key
	 */
	public static byte[] fullDecode(byte[] bytes, String key, int passes) throws IllegalArgumentException {
		bytes = decode(bytes, key, passes);
		if (bytes.length < 4 || bytes[0] != 0x00 || bytes[1] != 0x00 || bytes[2] != 0x00 || bytes[3] != 0x00) throw new IllegalArgumentException("This isn't valid byte array or key");
		return Arrays.copyOfRange(bytes, 4, bytes.length);
	}
	
	/**
	 * Decode given array with default amount of 3 passes, check validity and get properly trimmed data.
	 * 
	 * @param bytes bytes to full decode
	 * @param key key used for encoding
	 * @return ready to read decoded array
	 * @throws IllegalArgumentException when the array cannot be properly decoded using given key
	 */
	public static byte[] fullDecode(byte[] bytes, String key) throws IllegalArgumentException {
		return fullDecode(bytes, key, PASSES_COUNT);
	}
	
	/**
	 * Append control bytes to start of the array, encode new creation and store in given data output.
	 * 
	 * @param out output to store result
	 * @param bytes bytes to full encode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 */
	public static void encodeBytesToOutput(ByteArrayDataOutput out, byte[] bytes, String key, int passes) {
		bytes = fullEncode(bytes, key, passes);
		out.writeShort(bytes.length);
		out.write(bytes);
	}
	
	/**
	 * Append control bytes to start of the array, encode new creation with default amount of 3 passes and store in given data output.
	 * 
	 * @param out output to store result
	 * @param bytes bytes to full encode
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 */
	public static void encodeBytesToOutput(ByteArrayDataOutput out, byte[] bytes, String key) {
		encodeBytesToOutput(out, bytes, key, PASSES_COUNT);
	}
	
	/**
	 * Get byte array from given input, decode it, check validity and get properly trimmed data.
	 * 
	 * @param in input to get bytes from
	 * @param key key used for encoding
	 * @param passes amount of operation passes
	 * @return ready to read decoded array
	 * @throws IllegalArgumentException when the array cannot be properly decoded using given key
	 */
	public static byte[] decodeBytesFromInput(ByteArrayDataInput in, String key, int passes) throws IllegalArgumentException {
		byte[] bytes = new byte[in.readShort()];
		in.readFully(bytes);
		bytes = fullDecode(bytes, key, passes);
		return bytes;
	}
	
	/**
	 * Get byte array from given input, decode it with default amount of 3 passes, check validity and get properly trimmed data.
	 * 
	 * @param in input to get bytes from
	 * @param key key used for encoding
	 * @return ready to read decoded array
	 * @throws IllegalArgumentException when the array cannot be properly decoded using given key
	 */
	public static byte[] decodeBytesFromInput(ByteArrayDataInput in, String key) throws IllegalArgumentException {
		return decodeBytesFromInput(in, key, PASSES_COUNT);
	}
	

}
