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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

public class BukkitUtils {
	
	
	private static Class<?> Damageable;
	private static Method Damageable_setDamage;
	private static Method ItemMeta_displayName;
	private static Method ItemMeta_setDisplayNameComponent;
	private static Method ItemMeta_setDisplayName;
	//
	private static Method ItemMeta_lore;
	private static Method ItemMeta_setLoreComponents;
	private static Method ItemMeta_setLore;
	
	
	private static final boolean LEGACY_MATERIALS = Material.getMaterial("RED_WOOL") == null;
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
	private static final BungeeComponentSerializer BUNGEE = BungeeComponentSerializer.get();
	private static final Pattern ITEM_PATTERN = Pattern.compile("(\\*)?([a-zA-Z_]+)(:(0|[1-9]\\d*))?(#([a-fA-F0-9]{6}))?(\\|((?:[A-Za-z\\d+/]{4})*(?:[A-Za-z\\d+/]{3}=|[A-Za-z\\d+/]{2}==)?))? ?([1-9]\\d*)?");
	private static final Function<String, Optional<ItemStack>> ITEM_PARSER = LEGACY_MATERIALS ? BukkitUtils::parseLegacyItem : BukkitUtils::parseNewItem;
	
	
	static {
		try {Damageable = Class.forName("org.bukkit.inventory.meta.Damageable");} catch (ClassNotFoundException e) {}
		try {Damageable_setDamage = Damageable.getMethod("setDamage", Integer.TYPE);} catch (NullPointerException | NoSuchMethodException | SecurityException e) {}
		try {ItemMeta_displayName = ItemMeta.class.getMethod("displayName", Component.class);} catch (NoSuchMethodException | SecurityException e) {}
		try {ItemMeta_setDisplayNameComponent = ItemMeta.class.getMethod("setDisplayNameComponent", BaseComponent[].class);} catch (NoSuchMethodException | SecurityException e) {}
		try {ItemMeta_setDisplayName = ItemMeta.class.getMethod("setDisplayName", String.class);} catch (NoSuchMethodException | SecurityException e) {}
		//
		try {ItemMeta_lore = ItemMeta.class.getMethod("lore", List.class);} catch (NoSuchMethodException | SecurityException e) {}
		try {ItemMeta_setLoreComponents = ItemMeta.class.getMethod("setLoreComponents", List.class);} catch (NoSuchMethodException | SecurityException e) {}
		try {ItemMeta_setLore = ItemMeta.class.getMethod("setLore", List.class);} catch (NoSuchMethodException | SecurityException e) {}
	}
	
	
	public static void setDisplayName(ItemMeta meta, Component display) {
		try {
			if (ItemMeta_displayName != null) ItemMeta_displayName.invoke(meta, display);
			else if (ItemMeta_setDisplayNameComponent != null) ItemMeta_setDisplayNameComponent.invoke(meta, (Object) BUNGEE.serialize(display));
			else ItemMeta_setDisplayName.invoke(meta, LEGACY.serialize(display));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void setLore(ItemMeta meta, List<Component> lore) {
		try {
			if (ItemMeta_lore != null) ItemMeta_lore.invoke(meta, lore);
			else if (ItemMeta_setLoreComponents != null) ItemMeta_setDisplayNameComponent.invoke(meta, lore.stream().map(BUNGEE::serialize).collect(Collectors.toList()));
			else ItemMeta_setLore.invoke(meta, lore.stream().map(LEGACY::serialize).collect(Collectors.toList()));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	public static Optional<ItemStack> parseItem(@NotNull String text) throws IllegalArgumentException {
		return ITEM_PARSER.apply(text);
	}
	
	
	
	private static Optional<ItemStack> parseNewItem(@NotNull String text) throws IllegalArgumentException {
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
			if (damage != null && Damageable.isInstance(damage)) {
				try {
					Damageable_setDamage.invoke(meta, damage);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			item.setItemMeta(meta);
		}
		
		return Optional.of(item);
	}
	
	private static Optional<ItemStack> parseLegacyItem(@NotNull String text) throws IllegalArgumentException {
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
