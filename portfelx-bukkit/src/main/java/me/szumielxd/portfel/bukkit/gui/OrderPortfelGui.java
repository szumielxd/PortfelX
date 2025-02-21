package me.szumielxd.portfel.bukkit.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Range;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.api.configuration.BukkitConfigKey;
import me.szumielxd.portfel.bukkit.api.objects.OrderData;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.Availability;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.bukkit.utils.BukkitUtils;
import me.szumielxd.portfel.bukkit.utils.ComponentUtils;
import me.szumielxd.portfel.bukkit.utils.PlaceholderUtils;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class OrderPortfelGui implements AbstractPortfelGui {
	
	
	private final PortfelBukkitImpl plugin;
	private final String name;
	private final int slot;
	private final int size;
	private final @NotNull OrderGuiDisplay display;
	private final ShopType type;
	private final Map<Integer, OrderData> orders;
	
	
	public OrderPortfelGui(@NotNull PortfelBukkitImpl plugin, @NotNull String name, int slot, int rows, @NotNull OrderGuiDisplay display, @NotNull ShopType type, @NotNull List<OrderData> orders) {
		this.plugin = plugin;
		this.name = name;
		this.slot = slot;
		this.size = rows * 9;
		this.display = display;
		this.type = type;
		Range<Integer> range = Range.closed(0, this.getSize() - 1);
		this.orders = orders.stream()
				.filter(o -> range.contains(o.getSlot()))
				.collect(Collectors.toMap(OrderData::getSlot, Function.identity(), (a,b) -> b));
	}
	
	
	public @NotNull String getName() {
		return this.name;
	}
	
	@Override
	public @NotNull Component getTitle(@NotNull User user, @NotNull Player player) {
		return this.display.title()
				.placeholders(PlaceholderUtils.userPlaceholders(user))
				.buildComponent(BukkitSender.wrap(plugin, player));
	}
	
	public int getSlot() {
		return this.slot;
	}

	@Override
	public int getSize() {
		return this.size;
	}
	
	public @NotNull MessageDraft getDisplayName() {
		return this.display.displayName();
	}
	
	public @NotNull List<MessageDraft> getDescription() {
		return Collections.unmodifiableList(this.display.description());
	}
	
	public @NotNull ItemStack getIcon() {
		return this.display.icon().clone();
	}

	@Override
	public void onClick(@NotNull Player player, int slot) {
		OrderData order = this.orders.get(slot);
		if (order != null) {
			BukkitOperableUser user = (BukkitOperableUser) this.plugin.getUserManager().getUser(player.getUniqueId());
			if (user != null && order.getConditions().checkAvailability(player) == Availability.AVAILABLE) {
				long price = order.getPrice();
				if (this.type.equals(ShopType.UPGRADE)) {
					List<OrderData> orderList = orders.entrySet().stream()
							.map(Entry::getValue)
							.sorted(Comparator.comparingInt(OrderData::getLevel))
							.toList();
					int index = orderList.indexOf(order);
					if (index < 0) return;
					for (int i = index - 1; i >= 0; i--) {
						OrderData o = orderList.get(i);
						if (o.getConditions().checkAvailability(player) != Availability.DONE) break;
						price += o.getPrice();
					}
				}
				if (price > user.getBalance() && !user.inTestmode()) {
					Stream.of(Sound.values())
							.filter(s -> s.name().equals("ENTITY_VILLAGER_NO")||s.name().equals("VILLAGER_NO"))
							.findAny()
							.ifPresent(sound -> player.playSound(player.getLocation(), sound, 2, 1));
					return;
				}
				PortfelGuiHolder newHolder = new PortfelGuiHolder(this.plugin, new ConfirmOrderPortfelGui(this.plugin, order.onAirWithPrice(price)), user, player);
				newHolder.getGui().setup(player, newHolder.getInventory());
			}
		}
	}

	@Override
	public void setup(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.clear();
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user == null) return;
		var ordersArray = orders.entrySet().stream()
				.map(Entry::getValue)
				.sorted(Comparator.comparingInt(OrderData::getLevel))
				.toArray(OrderData[]::new);
		
		switch (this.type) {
			case NORMAL -> buildNormalIcons(ordersArray, player, user, inventory);
			case UPGRADE -> buildUpgradeIcons(ordersArray, player, user, inventory);
		}
		player.openInventory(inventory);
	}
	
	public @NotNull ShopType getType() {
		return this.type;
	}
	
	private void buildNormalIcons(@NotNull OrderData[] ordersArray, @NotNull Player player, @NotNull User user, @NotNull Inventory inventory) {
		for (int i = ordersArray.length - 1; i >= 0; i--) {
			OrderData order = ordersArray[i];
			inventory.setItem(order.getSlot(), buildIcon(order, player, user, order.getConditions().checkAvailability(player), order.getPrice(), List.of()));
		}
	}
	
	private void buildUpgradeIcons(@NotNull OrderData[] ordersArray, @NotNull Player player, @NotNull User user, @NotNull Inventory inventory) {
		boolean purchased = false;
		for (int i = ordersArray.length - 1; i >= 0; i--) {
			OrderData order = ordersArray[i];
			var availability = order.getConditions().checkAvailability(player);
			purchased |= availability == Availability.DONE;
			availability = availability == Availability.AVAILABLE && purchased ? Availability.DONE : availability;
			var discounts = calculateDiscount(Arrays.copyOf(ordersArray, i), player);
			var price = order.getPrice() + discounts.stream()
					.filter(d -> !d.active())
					.mapToLong(PriceDiscount::value)
					.sum();
			inventory.setItem(order.getSlot(), buildIcon(order, player, user, availability, price, discounts));
		}
	}
	
	private @NotNull ItemStack buildIcon(final @NotNull OrderData order, final @NotNull Player player, final @NotNull User user, @NotNull Availability availability, long price, @NotNull List<PriceDiscount> discounts) {
		this.plugin.debug("normalItem(%s) %s|%s availability: %s", player.getName(), this.getName(), order.getName(), availability);
		var wrapper = BukkitSender.wrap(plugin, player);
		ItemStack item = switch (availability) {
			case AVAILABLE -> order.getDisplay().icons().icon().clone();
			case DONE -> order.getDisplay().icons().iconDone().clone();
			case DENIED -> order.getDisplay().icons().iconDenied().clone();
		};
		ItemMeta meta = item.getItemMeta();
		BukkitUtils.setDisplayName(meta, MessageDraft.minimessage(order.getDisplay().displayName())
				.placeholders(PlaceholderUtils.userPlaceholders(user))
				.buildComponent(wrapper));
		
		BukkitUtils.setLore(meta, List.of(ComponentUtils.splitByNewline(BukkitLangKey.SHOP_ORDER_LORE.draft(
				buildPriceBlock(user, price, discounts),
				buildDescriptionMessageLines(order),
				buildDeniedDescriptionMessageLines(availability, order),
				buildStatusMessageLine(availability),
				BukkitLangKey.SHOP_ORDER_TOS_BLOCK
						.draft(this.plugin.getConfiguration().getString(BukkitConfigKey.SHOP_TERMS_OF_SERVICE)))
				.buildComponent(wrapper))));
		
		item.setItemMeta(meta);
		return item;
	}
	
	
	private @NotNull MessageDraft buildStatusMessageLine(@NotNull Availability availability) {
		return (switch (availability) {
			case AVAILABLE -> BukkitLangKey.SHOP_ORDER_STATUS_AVAILABLE;
			case DENIED -> BukkitLangKey.SHOP_ORDER_STATUS_DENIED;
			case DONE -> BukkitLangKey.SHOP_ORDER_STATUS_PURCHASED;
		}).draft();
	}
	
	
	private @NotNull MessageDraft buildDescriptionMessageLines(@NotNull OrderData order) {
		return Optional.ofNullable(order.getDisplay().description())
				.filter(l -> !l.isEmpty())
				.map(desc -> BukkitLangKey.SHOP_ORDER_DENIEDDESCRIPTION_BLOCK
						.draft(desc.stream()
								.map(MessageDraft::minimessage)
								.map(BukkitLangKey.SHOP_ORDER_DENIEDDESCRIPTION_LINEFORMAT::draft)
								.collect(MessageDraft.join(MessageDraft.newline()))))
				.orElse(MessageDraft.empty());
	}
	
	
	private @NotNull MessageDraft buildDeniedDescriptionMessageLines(@NotNull Availability availability, @NotNull OrderData order) {
		return Optional.ofNullable(order.getDisplay().denyDescription())
				.filter(l -> availability == Availability.DENIED && !l.isEmpty())
				.map(desc -> BukkitLangKey.SHOP_ORDER_DENIEDDESCRIPTION_BLOCK
						.draft(desc.stream()
								.map(MessageDraft::minimessage)
								.map(BukkitLangKey.SHOP_ORDER_DENIEDDESCRIPTION_LINEFORMAT::draft)
								.collect(MessageDraft.join(MessageDraft.newline()))))
				.orElse(MessageDraft.empty());
	}
	
	
	private @NotNull List<PriceDiscount> calculateDiscount(@NotNull OrderData[] discountOrders, @NotNull Player player) {
		List<PriceDiscount> discounts = new LinkedList<>();
		boolean active = false;
		for (int i = discountOrders.length - 1; i >= 0; i--) {
			active |= discountOrders[i].getConditions().checkAvailability(player) == Availability.DONE;
			discounts.add(0, new PriceDiscount(discountOrders[i], discountOrders[i].getPrice(), active));
		}
		return discounts;
	}
	
	private @NotNull MessageDraft buildPriceBlock(@NotNull User user, long price, @NotNull List<PriceDiscount> discounts) {
		List<MessageDraft> discountDrafts = new LinkedList<>();
		var iter = discounts.iterator();
		while (iter.hasNext()) {
			var discount = iter.next();
			LangKey discountLang;
			if (iter.hasNext()) {
				discountLang = discount.active() ?
						BukkitLangKey.SHOP_ORDER_PRICE_DISCOUNT_LINE_NORMAL_ACTIVE
						: BukkitLangKey.SHOP_ORDER_PRICE_DISCOUNT_LINE_NORMAL_INACTIVE;
			} else {
				discountLang = discount.active() ?
						BukkitLangKey.SHOP_ORDER_PRICE_DISCOUNT_LINE_LAST_ACTIVE
						: BukkitLangKey.SHOP_ORDER_PRICE_DISCOUNT_LINE_LAST_INACTIVE;
			}
			discountDrafts.add(discountLang.draft(
					MiniMessage.miniMessage().stripTags(discount.source().getDisplay().displayName()),
					discount.value()));
		}
		return formatColoredPrice(user, price)
				.append(discountDrafts.stream().collect(MessageDraft.join()));
	}
	
	private @NotNull MessageDraft formatColoredPrice(@NotNull User user, long price) {
		return (price < user.getBalance() ? BukkitLangKey.SHOP_ORDER_PRICE_NOTENOUGH : BukkitLangKey.SHOP_ORDER_PRICE_ENOUGH)
				.draft(MainLangKey.MAIN_CURRENCY_FORMAT.draft(price));
	}
	
	private record PriceDiscount(@NotNull OrderData source, long value, boolean active) {}
	
	public record OrderGuiDisplay(@NotNull MessageDraft title, @NotNull MessageDraft displayName, @NotNull List<MessageDraft> description, ItemStack icon) {}

}
