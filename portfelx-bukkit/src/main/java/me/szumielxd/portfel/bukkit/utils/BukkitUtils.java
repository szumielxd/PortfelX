package me.szumielxd.portfel.bukkit.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import lombok.experimental.UtilityClass;
import me.szumielxd.portfel.bukkit.objects.BukkitPlayer;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.KyoriUtils;
import me.szumielxd.portfel.common.utils.ReflectionUtils;

@UtilityClass
public class BukkitUtils {
	
	
	private final @Nullable Class<?> DAMAGEABLE_CLAZZ = ReflectionUtils.tryGetClass("org.bukkit.inventory.meta.Damageable");
	private final @Nullable Method DAMAGEABLE_SETDAMAGE = ReflectionUtils.tryGetMethod(DAMAGEABLE_CLAZZ, "setDamage", Integer.TYPE);
	//
	private final @Nullable Method ITEMMETA_DISPLAYNAME = ReflectionUtils.tryGetMethod(ItemMeta.class, "displayName", KyoriUtils.COMPONENT_CLAZZ);
	private final @Nullable Method ITEMMETA_SETDISPLAYNAME = ReflectionUtils.tryGetMethod(ItemMeta.class, "setDisplayName", String.class);
	//
	private final @Nullable Method ITEMMETA_LORE = ReflectionUtils.tryGetMethod(ItemMeta.class, "lore", List.class);
	private final @Nullable Method ITEMMETA_SETLORE = ReflectionUtils.tryGetMethod(ItemMeta.class, "setLore", List.class);
	//
	private final @Nullable Method ITEMMETA_SETGLINT = ReflectionUtils.tryGetMethod(ItemMeta.class, "setEnchantmentGlintOverride", Boolean.class);
	private final @Nullable Method ENCHANTMENT_GETTARGET = ReflectionUtils.tryGetMethod(Enchantment.class, "getItemTarget");
	
	private final boolean LEGACY_MATERIALS = Material.getMaterial("RED_WOOL") == null;
	private final Pattern ITEM_PATTERN = Pattern.compile("(\\*)?([a-zA-Z_]+)(:(0|[1-9]\\d*))?(#([a-fA-F0-9]{6}))?(\\|((?:[A-Za-z\\d+/]{4})*(?:[A-Za-z\\d+/]{3}=|[A-Za-z\\d+/]{2}==)?))? ?([1-9]\\d*)?");
	private final Function<ItemOptions, ItemStack> ITEM_CONSTRUCTOR = LEGACY_MATERIALS ? BukkitUtils::buildLegacyItem : BukkitUtils::buildNewItem;
	
	public void setDisplayName(@NotNull ItemMeta meta, @NotNull BukkitPlayer player, @Nullable MessageDraft display) {
		try {
			if (ITEMMETA_DISPLAYNAME != null) {
				ITEMMETA_DISPLAYNAME.invoke(meta, display == null ? null : KyoriUtils.toCommonKyori(display.buildComponent(player)));
			} else {
				ITEMMETA_SETDISPLAYNAME.invoke(meta, display == null ? null : display.buildLegacy(player));
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setLore(@NotNull ItemMeta meta, @NotNull BukkitPlayer player, @Nullable MessageDraft lore) {
		try {
			if (ITEMMETA_LORE != null) {
				ITEMMETA_LORE.invoke(meta, lore == null ? null : lore.buildComponentList(player).stream()
						.map(KyoriUtils::toCommonKyori)
						.toList());
			} else {
				ITEMMETA_SETLORE.invoke(meta, lore == null ? null : lore.buildLegacyList(player));
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setGlowing(@NotNull ItemMeta meta, Material mat) {
		try {
			if (ITEMMETA_SETGLINT != null) {
				ITEMMETA_SETGLINT.invoke(meta, true);
			} else {
				EnchantmentTarget target = (EnchantmentTarget) ENCHANTMENT_GETTARGET.invoke(Enchantment.LURE);
				meta.addEnchant(target.includes(mat) ? Enchantment.LURE : Enchantment.PROJECTILE_PROTECTION, 1, true);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setSkin(@NotNull SkullMeta meta, @NotNull String skin) {
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
	
	
	public @NotNull Optional<ItemStack> parseItem(@NotNull String text) throws IllegalArgumentException {
		if (text.isEmpty()) {
			return Optional.empty();
		}
		Matcher match = ITEM_PATTERN.matcher(text);
		if (!match.matches()) {
			throw new IllegalArgumentException("Malformed text");
		}
		Material mat = Material.matchMaterial(match.group(2));
		if (mat == null) {
			return Optional.empty();
		}
		
		Integer damage = null;
		int amount = 1;
		boolean glowing = false;
		Color color = null;
		String skin = null;
		
		if (match.group(4) != null) {
			damage = Integer.parseInt(match.group(4)); // data|damage
		}
		if (match.group(9) != null) {
			amount = Integer.parseInt(match.group(9)); // amount
		}
		if (match.group(1) != null) {
			glowing = true; // glowing
		}
		if (match.group(6) != null) {
			color = Color.fromRGB(Integer.parseInt(match.group(6), 16)); // color
		}
		if (match.group(8) != null) {
			skin = match.group(8); // skin
		}
		return Optional.of(ITEM_CONSTRUCTOR.apply(new ItemOptions(mat, amount, damage, glowing, color, skin)));
	}
	
	
	private @NotNull ItemStack buildNewItem(@NotNull ItemOptions options) {
		ItemStack item = new ItemStack(options.material(), options.amount());
		ItemMeta meta = item.getItemMeta();
		if (options.glowing()) {
			setGlowing(meta, options.material());
		}
		if (options.color() != null && meta instanceof LeatherArmorMeta leather) {
			leather.setColor(options.color());
		}
		if (options.skin() != null && meta instanceof SkullMeta skull) {
			setSkin(skull, options.skin());
		}
		if (options.damage() != null && DAMAGEABLE_CLAZZ.isInstance(options.damage())) {
			try {
				DAMAGEABLE_SETDAMAGE.invoke(meta, options.damage());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		item.setItemMeta(meta);
		return item;
	}
	
	private @NotNull ItemStack buildLegacyItem(@NotNull ItemOptions options) {
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(options.material(), options.amount(), options.data());
		ItemMeta meta = item.getItemMeta();
		if (options.glowing()) {
			setGlowing(meta, options.material());
		}
		if (options.color() != null && meta instanceof LeatherArmorMeta leather) {
			leather.setColor(options.color());
		}
		if (options.skin() != null && meta instanceof SkullMeta skull) {
			setSkin(skull, options.skin());
		}
		item.setItemMeta(meta);
		return item;
	}
	
	private record ItemOptions(@NotNull Material material, int amount, @Nullable Integer damage, boolean glowing, @Nullable Color color, @Nullable String skin) {
		public short data() {
			return (short) (damage == null ? 0 : damage);
		}
	}
	

}
