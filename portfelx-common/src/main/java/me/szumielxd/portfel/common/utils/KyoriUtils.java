package me.szumielxd.portfel.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

@UtilityClass
public class KyoriUtils {
	
	
	private static final String ADVENTURE_PATH = "net/kyori/adventure".replace('/', '.');
	
	public static final @Nullable Class<?> COMPONENT_CLAZZ = Optional.of(ADVENTURE_PATH+".text.Component").map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { return null; } } ).orElse(null);
	public static final @Nullable Class<?> COMPONENTLIKE_CLAZZ = Optional.of(ADVENTURE_PATH+".text.ComponentLike").map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { return null; } } ).orElse(null);
	private static final @Nullable Class<?> GSONSERIALIZER_CLAZZ = Optional.of(ADVENTURE_PATH+".text.serializer.gson.GsonComponentSerializer").map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { return null; } } ).orElse(null);
	private static final @Nullable Object GSON = Optional.ofNullable(GSONSERIALIZER_CLAZZ).map(clazz -> { try { return clazz.getMethod("gson").invoke(null); } catch(Exception e) { return null; }}).orElse(null);
	
	
	public static @NotNull Object toCommonKyori(@NotNull Component component) {
		if (GSONSERIALIZER_CLAZZ == null) throw new IllegalStateException("cannot find native adventure-api");
		Objects.requireNonNull(component, "component cannot be null");
		JsonElement json = GsonComponentSerializer.gson().serializeToTree(component);
		try {
			return GSONSERIALIZER_CLAZZ.getMethod("deserializeFromTree", JsonElement.class).invoke(GSON, json);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static @NotNull Component fromCommonKyori(@NotNull Object component) {
		if (GSONSERIALIZER_CLAZZ == null) throw new IllegalStateException("cannot find native adventure-api");
		Objects.requireNonNull(component, "component cannot be null");
		if (!COMPONENT_CLAZZ.isInstance(component)) throw new IllegalArgumentException(String.format("component must be instance of `%s`", COMPONENT_CLAZZ.getName()));
		try {
			JsonElement json = (JsonElement) GSONSERIALIZER_CLAZZ.getMethod("serializeToTree", JsonElement.class).invoke(GSON, component);
			return GsonComponentSerializer.gson().deserializeFromTree(json);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	

}
