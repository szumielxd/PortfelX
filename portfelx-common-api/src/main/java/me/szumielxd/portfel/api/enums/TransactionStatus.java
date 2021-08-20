package me.szumielxd.portfel.api.enums;

import java.util.Optional;
import java.util.stream.Stream;

public enum TransactionStatus {
	
	OK("Ok"),
	ERROR("Error"),
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
	
	
}