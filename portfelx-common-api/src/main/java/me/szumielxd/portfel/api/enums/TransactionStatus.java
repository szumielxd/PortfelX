package me.szumielxd.portfel.api.enums;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.objects.User;

public enum TransactionStatus {
	
	OK("Ok"),
	ERROR("Error"),
	NOT_LOADED("NotLoaded"),
	;
	
	
	private final String text;
	
	private TransactionStatus(String text) {
		this.text = text;
	}
	
	
	public String getText() {
		return text;
	}
	
	
	public static Optional<TransactionStatus> parse(String text) {
		return Stream.of(TransactionStatus.values()).filter(t -> t.text.equalsIgnoreCase(text)).findAny();
	}
	
	public static TransactionStatus wrap(@Nullable User user, @Nullable Throwable throwable) {
		if (throwable != null) {
			return ERROR;
		}
		if (user != null) {
			return OK;
		}
		return NOT_LOADED;
	}
	
	
}