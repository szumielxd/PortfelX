package me.szumielxd.portfel.common.communication.coders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jetbrains.annotations.NotNull;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageEntry {

	@NotNull MessageEntryCoders value();
	
}
