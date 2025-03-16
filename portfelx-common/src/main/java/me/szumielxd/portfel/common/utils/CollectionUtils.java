package me.szumielxd.portfel.common.utils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtils {
	
	/**
	 * Append null element to end of the first array.
	 * 
	 * @param array the array
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array) {
		return mergeArrays(array, null);
	}
	
	/**
	 * Append given <b>toAppend</b> element to end of the first array.
	 * 
	 * @param array first array
	 * @param toAppend element to append
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array, @Nullable T toAppend) {
		return mergeArrays(array, Arrays.asList(toAppend).toArray(array.clone()));
	}
	
	/**
	 * Append given <b>toAppend</b> array to end of the first array.
	 * 
	 * @param array first array
	 * @param toAppend array to append
	 * @return copy of array containing all merged elements
	 */
	public static <T> @NotNull T[] mergeArrays(@NotNull T[] array, @NotNull T[] toAppend) {
		T[] newArray = Arrays.copyOf(array, array.length + toAppend.length);
		System.arraycopy(toAppend, 0, newArray, array.length, toAppend.length);
		return newArray;
	}
	
	/**
	 * remove first element from the array.
	 * 
	 * @param array the array
	 * @return new modified array
	 */
	public static <T> @NotNull T[] popArray(@NotNull T[] array) {
		return popArray(array, 1);
	}
	
	/**
	 * remove <b>amount</b> of first elements from the array.
	 * 
	 * @param array the array
	 * @param amount amount of elements to remove
	 * @return new modified array
	 */
	public static <T> @NotNull T[] popArray(@NotNull T[] array, int amount) {
		if (amount > array.length) {
			amount = array.length;
		}
		return Arrays.copyOfRange(array, amount, array.length);
	}
	
	public static <E extends Enum<E>, T> EnumMap<E, T> mapOfEachEnum(Class<E> type, Function<E, T> valueGenerator) {
		return Stream.of(type.getEnumConstants())
				.collect(Collectors.toMap(
						Function.identity(),
						valueGenerator,
						(a, b) -> a,
						() -> new EnumMap<>(type)));
	}

}
