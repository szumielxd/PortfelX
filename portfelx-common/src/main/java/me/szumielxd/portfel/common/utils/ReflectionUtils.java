package me.szumielxd.portfel.common.utils;

import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {
	
	
	public @Nullable Class<?> tryGetClass(@NotNull String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	
	public @Nullable Method tryGetMethod(@Nullable Class<?> owner, @NotNull String methodName, Class<?>... argTypes) {
		if (owner != null) {
			try {
				return owner.getMethod(methodName, argTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				// nothing
			}
		}
		return null;
	}
	

}
