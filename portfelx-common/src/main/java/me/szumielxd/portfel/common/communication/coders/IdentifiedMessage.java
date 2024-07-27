package me.szumielxd.portfel.common.communication.coders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jetbrains.annotations.NotNull;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdentifiedMessage {
	
	@NotNull String value();

}
