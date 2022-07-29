package me.szumielxd.portfel.bukkit.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import lombok.experimental.UtilityClass;
import me.szumielxd.portfel.common.utils.KyoriUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

@UtilityClass
public class BukkitUtils {
	
	
	private static final @Nullable Class<?> DAMAGEABLE_CLAZZ = Optional.of("org.bukkit.inventory.meta.Damageable").map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { return null; } }).orElse(null);
	private static final @Nullable Method DAMAGEABLE_SETDAMAGE = Optional.ofNullable(DAMAGEABLE_CLAZZ).map(clazz -> { try { return clazz.getMethod("setDamage", Integer.TYPE); } catch (Exception e) { return null; } }).orElse(null);
	//
	private static final @Nullable Method ITEMMETA_DISPLAYNAME = Optional.ofNullable(KyoriUtils.COMPONENT_CLAZZ).map(clazz -> { try { return ItemMeta.class.getMethod("displayName", clazz); } catch (Exception e) { return null; } }).orElse(null);
	private static final @Nullable Method ITEMMETA_SETDISPLAYNAMECOMPONENT = Optional.ofNullable(BaseComponent[].class).map(clazz -> { try { return ItemMeta.class.getMethod("setDisplayNameComponent", clazz); } catch (Exception e) { return null; } }).orElse(null);
	private static final @Nullable Method ITEMMETA_SETDISPLAYNAME = Optional.ofNullable(String.class).map(clazz -> { try { return ItemMeta.class.getMethod("setDisplayName", clazz); } catch (Exception e) { return null; } }).orElse(null);
	//
	private static final @Nullable Method ITEMMETA_LORE = Optional.ofNullable(List.class).map(clazz -> { try { return ItemMeta.class.getMethod("lore", clazz); } catch (Exception e) { return null; } }).orElse(null);
	private static final @Nullable Method ITEMMETA_SETLORECOMPONENTS = Optional.ofNullable(List.class).map(clazz -> { try { return ItemMeta.class.getMethod("setLoreComponents", clazz); } catch (Exception e) { return null; } }).orElse(null);
	private static final @Nullable Method ITEMMETA_SETLORE = Optional.ofNullable(List.class).map(clazz -> { try { return ItemMeta.class.getMethod("setLore", clazz); } catch (Exception e) { return null; } }).orElse(null);
	
	private static final boolean LEGACY_MATERIALS = Material.getMaterial("RED_WOOL") == null;
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
	private static final BungeeComponentSerializer BUNGEE = BungeeComponentSerializer.get();
	private static final Pattern ITEM_PATTERN = Pattern.compile("(\\*)?([a-zA-Z_]+)(:(0|[1-9]\\d*))?(#([a-fA-F0-9]{6}))?(\\|((?:[A-Za-z\\d+/]{4})*(?:[A-Za-z\\d+/]{3}=|[A-Za-z\\d+/]{2}==)?))? ?([1-9]\\d*)?");
	private static final Function<String, Optional<ItemStack>> ITEM_PARSER = LEGACY_MATERIALS ? BukkitUtils::parseLegacyItem : BukkitUtils::parseNewItem;
	
	
	public static void setDisplayName(@NotNull ItemMeta meta, @Nullable Component display) {
		Objects.requireNonNull(meta, "meta cannot be null");
		try {
			if (ITEMMETA_DISPLAYNAME != null) ITEMMETA_DISPLAYNAME.invoke(meta, display == null ? null : KyoriUtils.toCommonKyori(display));
			else if (ITEMMETA_SETDISPLAYNAMECOMPONENT != null) ITEMMETA_SETDISPLAYNAMECOMPONENT.invoke(meta, display == null ? null : (Object) BUNGEE.serialize(display));
			else ITEMMETA_SETDISPLAYNAME.invoke(meta, display == null ? null : LEGACY.serialize(display));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void setLore(@NotNull ItemMeta meta, @Nullable List<Component> lore) {
		Objects.requireNonNull(meta, "meta cannot be null");
		try {
			if (ITEMMETA_LORE != null) ITEMMETA_LORE.invoke(meta, lore == null ? null : lore.stream().map(KyoriUtils::toCommonKyori).collect(Collectors.toList()));
			else if (ITEMMETA_SETLORECOMPONENTS != null) ITEMMETA_SETLORECOMPONENTS.invoke(meta, lore == null ? null : lore.stream().map(BUNGEE::serialize).collect(Collectors.toList()));
			else ITEMMETA_SETLORE.invoke(meta, lore == null ? null : lore.stream().map(LEGACY::serialize).collect(Collectors.toList()));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static Optional<ItemStack> parseItem(@NotNull String text) throws IllegalArgumentException {
		return ITEM_PARSER.apply(text);
	}
	
	
	
	private static @NotNull Optional<ItemStack> parseNewItem(@NotNull String text) throws IllegalArgumentException {
		if (Objects.requireNonNull(text, "text cannot be null").isEmpty()) return Optional.empty();
		Matcher match = ITEM_PATTERN.matcher(text);
		if (!match.matches()) throw new IllegalArgumentException("Malformed text");
		Material mat = Material.matchMaterial(match.group(2));
		if (mat == null) return Optional.empty();
		
		Integer damage = null;
		int amount = 1;
		boolean glowing = false;
		Color color = null;
		String skin = null;
		
		if (match.group(4) != null) damage = Integer.parseInt(match.group(4)); // data|damage
		if (match.group(9) != null) amount = Integer.parseInt(match.group(9)); // amount
		if (match.group(1) != null) glowing = true; // glowing
		if (match.group(6) != null) color = Color.fromRGB(Integer.parseInt(match.group(6), 16)); // color
		if (match.group(8) != null) skin = match.group(8); // skin
		
		ItemStack item = new ItemStack(mat, amount); {
			ItemMeta meta = item.getItemMeta();
			if (glowing) {
				item.addUnsafeEnchantment(Enchantment.LURE.getItemTarget().includes(item) ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.LURE, 1);
				meta = item.getItemMeta();
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			if (color != null && meta instanceof LeatherArmorMeta) {
				LeatherArmorMeta leather = (LeatherArmorMeta) meta;
				leather.setColor(color);
			}
			if (skin != null && meta instanceof SkullMeta) {
				GameProfile profile = new GameProfile(UUID.randomUUID(), "");
				profile.getProperties().put("textures", new Property("textures", skin));
				try {
					Method meth = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
					meth.setAccessible(true);
					meth.invoke(meta, profile);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
					// fallback for older versions
					try {
						Field f = meta.getClass().getDeclaredField("profile");
						f.setAccessible(true);
						f.set(meta, profile);
					} catch (SecurityException | IllegalAccessException | NoSuchFieldException ex) {
						e.printStackTrace();
					}
				}
			}
			if (damage != null && DAMAGEABLE_CLAZZ.isInstance(damage)) {
				try {
					DAMAGEABLE_SETDAMAGE.invoke(meta, damage);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			item.setItemMeta(meta);
		}
		
		return Optional.of(item);
	}
	
	private static @NotNull Optional<ItemStack> parseLegacyItem(@NotNull String text) throws IllegalArgumentException {
		if (Objects.requireNonNull(text, "text cannot be null").isEmpty()) return Optional.empty();
		Matcher match = ITEM_PATTERN.matcher(text);
		if (!match.matches()) throw new IllegalArgumentException("Malformed text");
		Material mat = Material.matchMaterial(match.group(2));
		if (mat == null) return Optional.empty();
		
		short data = 0;
		int amount = 1;
		boolean glowing = false;
		Color color = null;
		String skin = null;
		
		if (match.group(4) != null) data = Short.parseShort(match.group(4)); // data|damage
		if (match.group(9) != null) amount = Integer.parseInt(match.group(9)); // amount
		if (match.group(1) != null) glowing = true; // glowing
		if (match.group(6) != null) color = Color.fromRGB(Integer.parseInt(match.group(6), 16)); // color
		if (match.group(8) != null) skin = match.group(8); // skin
		
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(mat, amount, data); {
			ItemMeta meta = item.getItemMeta();
			if (glowing) {
				item.addUnsafeEnchantment(Enchantment.LURE.getItemTarget().includes(item) ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.LURE, 1);
				meta = item.getItemMeta();
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			if (color != null && meta instanceof LeatherArmorMeta) {
				LeatherArmorMeta leather = (LeatherArmorMeta) meta;
				leather.setColor(color);
			}
			if (skin != null && meta instanceof SkullMeta) {
				GameProfile profile = new GameProfile(UUID.randomUUID(), "");
				profile.getProperties().put("textures", new Property("textures", skin));
				try {
					Method meth = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
					meth.setAccessible(true);
					meth.invoke(meta, profile);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			item.setItemMeta(meta);
		}
		
		return Optional.of(item);
	}
	

}
