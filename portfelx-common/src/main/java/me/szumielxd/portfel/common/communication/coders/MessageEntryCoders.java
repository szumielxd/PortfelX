package me.szumielxd.portfel.common.communication.coders;

import java.lang.reflect.Type;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.communication.coders.MessageEntryCoder.MessageEntryCoderCreator;

public enum MessageEntryCoders {

	BOOLEAN(MessageEntryCoder.BOOLEAN),
	INTEGER(MessageEntryCoder.INTEGER),
	LONG(MessageEntryCoder.LONG),
	ASCII(MessageEntryCoder.ASCII),
	UTF(MessageEntryCoder.UTF),
	UUID(MessageEntryCoder.UUID),
	OBJECT(MessageEntryCoder.OBJECT_FETCHER),
	ENUM(MessageEntryCoder.ENUM),
	CRYPTO(MessageEntryCoder.CRYPTO);
	
	private final @NotNull MessageEntryCoderCreator<?> coderCreator;
	
	private <T> MessageEntryCoders(@NotNull MessageEntryCoderCreator<T> coderCreator) {
		this.coderCreator = coderCreator;
	}
	
	public Optional<? extends MessageEntryCoder<?>> getIfValid(Type type) {
		return this.coderCreator.generateIfValid(type);
	}
	
	public boolean isApplicable(Type type) {
		return this.coderCreator.isApplicable(type);
	}

}
