package me.szumielxd.portfel.common.utils;

import java.util.function.Function;

import lombok.SneakyThrows;

public interface ExceptionalFunction<T, R> extends Function<T, R> {

	
	public R applyExceptionally(T value) throws Throwable;
	
	
	@SneakyThrows
	@Override
	public default R apply(T t) {
		return applyExceptionally(t);
	}
	
	
	public static <T, R> ExceptionalFunction<T, R> of(ExceptionalFunction<T, R> x) {
		return x;
	}
	

}
