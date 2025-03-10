package me.szumielxd.portfel.common.utils.future;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import lombok.SneakyThrows;

public interface ExceptionalSupplier<T> extends Supplier<T> {

	
	public T getExceptionally() throws Throwable;
	
	
	@SneakyThrows
	@Override
	public default T get() {
		return this.getExceptionally();
	}
	
	
	public static <E> CompletableFuture<E> supplyAsync(ExceptionalSupplier<E> run) {
		return CompletableFuture.supplyAsync(run);
	}
	

}
