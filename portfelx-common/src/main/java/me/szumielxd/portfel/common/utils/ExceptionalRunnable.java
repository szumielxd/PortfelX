package me.szumielxd.portfel.common.utils;

import java.util.concurrent.CompletableFuture;

import lombok.SneakyThrows;

public interface ExceptionalRunnable extends Runnable {

	
	public void runExceptionally() throws Throwable;
	
	
	@SneakyThrows
	@Override
	public default void run() {
		this.runExceptionally();
	}
	
	
	public static CompletableFuture<Void> runAsync(ExceptionalRunnable run) {
		return CompletableFuture.runAsync(run);
	}
	

}
