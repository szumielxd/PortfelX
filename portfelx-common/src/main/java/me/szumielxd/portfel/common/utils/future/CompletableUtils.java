package me.szumielxd.portfel.common.utils.future;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CompletableUtils {
	
	public <T> @NotNull Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> mergedCompletable() {
		return Collectors.collectingAndThen(Collectors.toList(), CompletableUtils::mergeCompletable);
	}
	
	public <T> @NotNull CompletableFuture<List<T>> mergeCompletable(@NotNull Collection<CompletableFuture<T>> collection) {
		return CompletableFuture.allOf(collection.toArray(CompletableFuture[]::new))
				.thenApply(x -> collection.stream()
						.map(CompletableFuture::join)
						.toList());
	}
	
	public <K, V> @NotNull Collector<Entry<K, CompletableFuture<V>>, ?, CompletableFuture<Map<K, V>>> mergedCompletableMap() {
		return Collectors.collectingAndThen(Collectors.toList(), CompletableUtils::mergeCompletableEntries);
	}
	
	public <K, V> @NotNull CompletableFuture<Map<K, V>> mergeCompletableEntries(@NotNull Collection<Entry<K, CompletableFuture<V>>> collection) {
		return CompletableFuture.allOf(collection.toArray(CompletableFuture[]::new))
				.thenApply(x -> collection.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().join())));
	}
	
	public <K, V> @NotNull CompletableFuture<Map<K, V>> mergeCompletableMap(@NotNull Map<K, CompletableFuture<V>> map) {
		return mergeCompletableEntries(map.entrySet());
	}
	
	public static <K, V, U extends CompletableFuture<V>> U insertAutoremovable(Map<K, U> map, K key, U value, BiConsumer<K, CompletableFuture<V>> onFinalize) {
		if (!map.containsKey(key)) {
			removeOnCompletion(map, key, value, onFinalize);
			if (map.putIfAbsent(key, value) == null) {
				return value;
			}
		}
		throw new IllegalStateException("Given key already exists in the map (%s)".formatted(key));
	}
	
	public static <K, V, U extends CompletableFuture<V>> U insertAutoremovable(Map<K, U> map, K key, U value) {
		return insertAutoremovable(map, key, value, (k, v) -> {});
	}
	
	public static <K, V> CompletableFuture<V> insertAutoremovable(Map<K, CompletableFuture<V>> map, K key, BiConsumer<K, CompletableFuture<V>> onFinalize) {
		return insertAutoremovable(map, key, new CompletableFuture<V>(), onFinalize);
	}
	
	public static <K, V> CompletableFuture<V> insertAutoremovable(Map<K, CompletableFuture<V>> map, K key) {
		return insertAutoremovable(map, key, (BiConsumer<K, CompletableFuture<V>>)(k, v) -> {});
	}
	
	public static <K, V> CompletableFuture<V> fetchOrRun(Map<K, CompletableFuture<V>> map, K key, BiConsumer<K, CompletableFuture<V>> onNewEntry, BiConsumer<K, CompletableFuture<V>> onFinalize) {
		return map.computeIfAbsent(key, id -> {
			var future = removeOnCompletion(map, key, onFinalize);
			onNewEntry.accept(key, future);
			return future;
		});
	}
	
	public static <K, V> CompletableFuture<V> fetchOrRun(Map<K, CompletableFuture<V>> map, K key, BiConsumer<K, CompletableFuture<V>> onNewEntry) {
		return fetchOrRun(map, key, onNewEntry, (k, v) -> {});
	}
	
	public static <K, V> CompletableFuture<V> fetchOrRun(Map<K, CompletableFuture<V>> map, K key) {
		return fetchOrRun(map, key, (k, v) -> {});
	}
	
	public static <K, V, U extends CompletableFuture<V>> U removeOnCompletion(Map<K, U> map, K key, U value, BiConsumer<K, CompletableFuture<V>> onFinalize) {
		value.orTimeout(5, TimeUnit.SECONDS);
		value.whenComplete((res, ex) -> {
			onFinalize.accept(key, value);
			map.remove(key, value);
		});
		return value;
	}
	
	public static <K, V> CompletableFuture<V> removeOnCompletion(Map<K, CompletableFuture<V>> map, K key, BiConsumer<K, CompletableFuture<V>> onFinalize) {
		return removeOnCompletion(map, key, new CompletableFuture<>(), onFinalize);
	}
	
	public static <V, E extends Throwable> BiConsumer<V, E> handleResult(Consumer<V> success, Consumer<E> exception) {
		return (v, e) -> {
			if (e != null) {
				exception.accept(e);
			} else {
				success.accept(v);
			}
		};
	}

}
