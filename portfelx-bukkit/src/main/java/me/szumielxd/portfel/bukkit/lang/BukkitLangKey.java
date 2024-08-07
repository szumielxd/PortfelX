package me.szumielxd.portfel.bukkit.lang;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.lang.Lang.LangKey;

@Getter
public enum BukkitLangKey implements LangKey {
	
	COMMAND_TESTMODE_DESCRIPTION("command.testmode.decription", "Toggle your test-mode state. When toggled on, allows you to purchase orders without charge. Disabled on disconnect."),
	COMMAND_TESTMODE_EXECUTE("command.testmode.execute", "<light_purple>You've toggled <aqua>{0}</aqua> portfel test-mode."),
	//
	COMMAND_SYSTEM_DESCRIPTION("command.system.description", "All portfel setup related commands."),
	//
	COMMAND_SYSTEM_RELOAD_DESCRIPTION("command.system.reload.description", "Reload partially plugin's configuration."),
	COMMAND_SYSTEM_RELOAD_EXECUTE("command.system.reload.execute", "<gray>Reloading plugin..."),
	COMMAND_SYSTEM_RELOAD_SUCCESS("command.system.reload.success", "<green>Successfully reloaded <aqua>{0} {1}."),
	COMMAND_SYSTEM_RELOAD_ERROR("command.system.reload.success", "<hover:show_text:<red>{0}><dark_red>An error occured while attempting to reload this plugin. Hover this text or see console for more informations."),
	//
	SHOP_TITLE("shop.title", "<bold><dark_purple>Wallet (<aqua>{0}</aqua>)"),

	SHOP_MAIN_LORE("shop.main.lore", "<gray>{0}\\n\\n<aqua>/{1} {2}"),
	
	// "price | description | denied | status | TOS"
	SHOP_ORDER_LORE("shop.order.lore", "<gray>{0}\\n{1}\\n{2}\\n{3}\\n{4}"),
	SHOP_ORDER_PRICE_ENOUGH("shop.order.price.enough", "<green>{0}"),
	SHOP_ORDER_PRICE_NOTENOUGH("shop.order.price.not-enough", "<red>{0}"),
	SHOP_ORDER_PRICE_DISCOUNT_LINE_NORMAL_ACTIVE("shop.order.price.discount.line-normal.active", "<white> ┣╸ <gray>{0} <aqua>{1}\\n"),
	SHOP_ORDER_PRICE_DISCOUNT_LINE_NORMAL_INACTIVE("shop.order.price.discount.line-normal.inactive", "<gray> ┣╸ <dark_gray>{0} <dark_aqua>{1}\\n"),
	SHOP_ORDER_PRICE_DISCOUNT_LINE_LAST_ACTIVE("shop.order.price.discount.line-last.active", "<white> ┗╸ <gray>{0} <aqua>{1}\\n"),
	SHOP_ORDER_PRICE_DISCOUNT_LINE_LAST_INACTIVE("shop.order.price.discount.line-last.inactive", "<gray> ┗╸ <dark_gray>{0} <dark_aqua>{1}\\n"),
	
	SHOP_ORDER_PRICE_BLOCK("shop.order.price.block", "<gray>Price: {0}\\n"),
	
	SHOP_ORDER_DENIEDDESCRIPTION_BLOCK("shop.order.denied-description.block", "\\n{0}\\n"),
	SHOP_ORDER_DENIEDDESCRIPTION_LINEFORMAT("shop.order.denied-description.line-format", "<red><italic>  {0}"),
	
	SHOP_ORDER_DESCRIPTION_BLOCK("shop.order.description.block", "Description:\\n{0}\\n"),
	SHOP_ORDER_DESCRIPTION_LINEFORMAT("shop.order.description.line-format", "<aqua>  {0}"),
	
	SHOP_ORDER_STATUS_PURCHASED("shop.order.status.purchased", "<green>Purchased"),
	SHOP_ORDER_STATUS_DENIED("shop.order.status.denied", "<red>Denied!"),
	SHOP_ORDER_STATUS_AVAILABLE("shop.order.status.denied", "<yellow>Click to buy!"),
	
	SHOP_ORDER_TOS_BLOCK("shop.order.tos.block", "Terms of service:\\n{0}"),
	//
	SHOP_CONFIRM_TITLE("shop.confirm.title", "<aqua>Are you sure? That's {0}!"),
	SHOP_CONFIRM_YES_TITLE("shop.confirm.yes.title", "<green>Yes"),
	SHOP_CONFIRM_YES_DESCRIPTION("shop.confirm.yes.description", "<gray>I'm sure i want to spend <aqua>{0}</aqua> for <aqua>{1}</aqua>!"),
	SHOP_CONFIRM_NO_TITLE("shop.confirm.no.title", "<dark_red>No"),
	SHOP_CONFIRM_NO_DESCRIPTION("shop.confirm.no.description", "<gray>Wait! I must pay for it <aqua>{0}</aqua>!?"),
	
	TESTMODE_NOTIFICATION("testmode.notification", "<red>You're in test-mode, all your transactions are completed without taking charge. Remember to disable test-mode after all done."),
	
	MAIN_CURRENCY_FORMAT("main.currrency.format", "${0}"),
	MAIN_MINORCURRENCY_FORMAT("main.minor-currrency.format", "⧉{0}"),
	MAIN_WARNING("main.warning", "<dark_red><bold>WARNING: </bold>{0}"),
	MAIN_VALUE_ON("main.value.on", "on"),
	MAIN_VALUE_OFF("main.value.off", "off"),
	
	;// END,
	
	private final String path;
	private final String defString;
	private final boolean modifiable;
	

	/**
	 * Lang enum constructor.
	 * 
	 * @param path The string path.
	 * @param start The default string.
	 */
	private BukkitLangKey(String path, String defString) {
		this(path, defString, true);
	}
	
	/**
	 * Lang enum constructor.
	 * 
	 * @param path The string path.
	 * @param start The default string.
	 * @param whether value should be loaded from file
	 */
	private BukkitLangKey(@NotNull String path, @NotNull String defString, boolean modifiable) {
		this.path = path;
		this.defString = defString;
		this.modifiable = modifiable;
	}

}
