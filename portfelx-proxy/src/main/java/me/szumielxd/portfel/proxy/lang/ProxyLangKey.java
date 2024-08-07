package me.szumielxd.portfel.proxy.lang;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.lang.Lang.LangKey;

@Getter
public enum ProxyLangKey implements LangKey {
	
	COMMAND_ARGTYPES_USER_DISPLAY("command.arg-types.user.display", "user"),
	COMMAND_ARGTYPES_USER_DESCRIPTION("command.arg-types.user.description", "user representated by name or unique id"),
	COMMAND_ARGTYPES_USER_ERROR("command.arg-types.user.error", "<red>A user for <dark_red>{0}</dark_red> could not be found."),
	//
	COMMAND_ARGTYPES_TOKEN_DISPLAY("command.arg-types.token.display", "giftcode"),
	COMMAND_ARGTYPES_TOKEN_DESCRIPTION("command.arg-types.token.description", "giftcode representated by token"),
	COMMAND_ARGTYPES_TOKEN_ERROR("command.arg-types.token.error", "<red>A giftcode for <dark_red>{0}</dark_red> could not be found."),
	//
	COMMAND_ARGTYPES_SERVERNAME_DISPLAY("command.arg-types.servername.display", "serverName"),
	COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION("command.arg-types.servername.description", "user-friendly text representation of server instance (not the same as bungee serverName)"),
	//
	COMMAND_ARGTYPES_HASHKEY_DISPLAY("command.arg-types.hashkey.display", "hashKey"),
	COMMAND_ARGTYPES_HASHKEY_DESCRIPTION("command.arg-types.hashkey.description", "hash provided by subject server used to encode messages (see `server-key.dat` file in client-server's walet directory)"),
	//
	COMMAND_ARGTYPES_SERVER_DISPLAY("command.arg-types.server.display", "server"),
	COMMAND_ARGTYPES_SERVER_DESCRIPTION("command.arg-types.server.description", "server representated by friendly-name or unique id"),
	COMMAND_ARGTYPES_SERVER_ERROR("command.arg-types.server.error", "<red>A server for <dark_red>{0}</dark_red> could not be found."),
	//
	COMMAND_ARGTYPES_ORDER_DISPLAY("command.arg-types.order.display", "order"),
	COMMAND_ARGTYPES_ORDER_DESCRIPTION("command.arg-types.order.description", "name of global order"),
	COMMAND_ARGTYPES_ORDER_ERROR("command.arg-types.order.error", "<red>An order for <dark_red>{0}</dark_red> could not be found. Remember to firstly create it in orders.yml file."),
	//
	COMMAND_ARGTYPES_ECO_AMOUNT_DISPLAY("command.arg-types.eco-amount.display", "amount"),
	COMMAND_ARGTYPES_ECO_AMOUNT_DESCRIPTION("command.arg-types.eco-amount.description", "amount of money"),
	COMMAND_ARGTYPES_ECO_AMOUNT_ERROR("command.arg-types.eco-amount.error", "<red>Hey! <dark_red>{0}</dark_red> is not valid amount. Remember amount is represented by an positive integer."),
	//
	COMMAND_ARGTYPES_REASON_DISPLAY("command.arg-types.reason.display", "reason"),
	COMMAND_ARGTYPES_REASON_DESCRIPTION("command.arg-types.reason.description", "reason of this action"),
	COMMAND_ARGTYPES_REASON_ERROR("command.arg-types.reason.error", "<red>You must provide a good reason to run this command."),
	//
	COMMAND_ARGTYPES_INTOP_DISPLAY("command.arg-types.in-top.display", "reason"),
	COMMAND_ARGTYPES_INTOP_DESCRIPTION("command.arg-types.in-top.description", "reason of this action"),
	COMMAND_ARGTYPES_INTOP_ERROR("command.arg-types.in-top.error", "<red>Did you know <dark_red>{0}</dark_red> is not true nor false?"),
	//
	COMMAND_ARGTYPES_PAGENUMBER_DISPLAY("command.arg-types.page-number.display", "page"),
	COMMAND_ARGTYPES_PAGENUMBER_DESCRIPTION("command.arg-types.page-number.description", "current page of logs"),
	//
	COMMAND_ARGTYPES_PAGESIZE_DISPLAY("command.arg-types.page-size.display", "size"),
	COMMAND_ARGTYPES_PAGESIZE_DESCRIPTION("command.arg-types.page-size.description", "size of one page"),
	COMMAND_ARGTYPES_PAGESIZE_ERROR("command.arg-types.page-size.error", "<red>Are you stupid or stupid? Page size of <dark_red>{0}</dark_red> is definitly unsafe. Max allowed is {1}."),
	//
	COMMAND_ARGTYPES_LOGTARGET_DISPLAY("command.arg-types.log-target.display", "targets"),
	COMMAND_ARGTYPES_LOGTARGET_DESCRIPTION("command.arg-types.log-target.description", "string representation of action's target"),
	//
	COMMAND_ARGTYPES_LOGEXECUTOR_DISPLAY("command.arg-types.log-executor.display", "executors"),
	COMMAND_ARGTYPES_LOGEXECUTOR_DESCRIPTION("command.arg-types.log-executor.description", "string representation of action's executor"),
	//
	COMMAND_ARGTYPES_LOGSERVER_DISPLAY("command.arg-types.log-executor.display", "servers"),
	COMMAND_ARGTYPES_LOGSERVER_DESCRIPTION("command.arg-types.log-executor.description", "string representation of server where action has place"),
	//
	COMMAND_ARGTYPES_LOGORDER_DISPLAY("command.arg-types.log-executor.display", "orders"),
	COMMAND_ARGTYPES_LOGORDER_DESCRIPTION("command.arg-types.log-executor.description", "string representation of order's name"),
	//
	COMMAND_ARGTYPES_LOGACTION_DISPLAY("command.arg-types.log-executor.display", "actions"),
	COMMAND_ARGTYPES_LOGACTION_DESCRIPTION("command.arg-types.log-executor.description", "string representation of action's type"),
	//
	COMMAND_ARGTYPES_LOGVALCOND_DISPLAY("command.arg-types.log-value-condition.display", "values"),
	COMMAND_ARGTYPES_LOGVALCOND_DESCRIPTION("command.arg-types.log-value-condition.description", "conditions describing range of values"),
	//
	COMMAND_ARGTYPES_LOGBALCOND_DISPLAY("command.arg-types.log-balance-condition.display", "balances"),
	COMMAND_ARGTYPES_LOGBALCOND_DESCRIPTION("command.arg-types.log-balance-condition.description", "conditions describing range of balances"),
	//
	COMMAND_ARGTYPES_TOKENCREATOR_DISPLAY("command.arg-types.token-creator.display", "creators"),
	COMMAND_ARGTYPES_TOKENCREATOR_DESCRIPTION("command.arg-types.token-creator.description", "string representation of order's creator"),
	//
	COMMAND_ARGTYPES_TOKENSERVER_DISPLAY("command.arg-types.token-server.display", "servers"),
	COMMAND_ARGTYPES_TOKENSERVER_DESCRIPTION("command.arg-types.token-server.description", "string representation of order's server"),
	//
	COMMAND_ARGTYPES_TOKENORDER_DISPLAY("command.arg-types.token-order.display", "orders"),
	COMMAND_ARGTYPES_TOKENORDER_DESCRIPTION("command.arg-types.token-order.description", "string representation of order's name"),
	//
	COMMAND_ARGTYPES_TOKENCREATECOND_DISPLAY("command.arg-types.token-createdate-condition.display", "creationDates"),
	COMMAND_ARGTYPES_TOKENCREATECOND_DESCRIPTION("command.arg-types.token-createdate-condition.description", "conditions describing range of creation dates"),
	//
	COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DISPLAY("command.arg-types.token-expirationdate-condition.display", "creationDates"),
	COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DESCRIPTION("command.arg-types.token-expirationdate-condition.description", "conditions describing range of expiration dates"),
	//
	COMMAND_ARGTYPES_GIFTORDER_DISPLAY("command.arg-types.gift-order.display", "giftOrder"),
	COMMAND_ARGTYPES_GIFTORDER_DESCRIPTION("command.arg-types.gift-order.description", "order called on gift-code execution"),
	//
	COMMAND_ARGTYPES_GIFTEXPIRATION_DISPLAY("command.arg-types.gift-expiration.display", "expiration"),
	COMMAND_ARGTYPES_GIFTEXPIRATION_DESCRIPTION("command.arg-types.gift-expiration.description", "expiration time of this gift, use -1 for lifetime token"),
	COMMAND_ARGTYPES_GIFTEXPIRATION_ERROR("command.arg-types.gift-expiration.error", "<red>That's pretty sure '<dark_red>{0}</dark_red>' isn't valid date format. Working examples: `1629864019000`, 10d"),
	//
	COMMAND_ARGTYPES_GIFTSERVERS_DISPLAY("command.arg-types.gift-servers.display", "servers"),
	COMMAND_ARGTYPES_GIFTSERVERS_DESCRIPTION("command.arg-types.gift-servers.description", "comma separated servers where gift can be used, use `*` for any server on proxy, of `+` for any server with registered portfel"),
	//
	COMMAND_ARGTYPES_GIFTTOKEN_DISPLAY("command.arg-types.gift-servers.display", "token"),
	COMMAND_ARGTYPES_GIFTTOKEN_DESCRIPTION("command.arg-types.gift-servers.description", "token used to obtain this gift, if not given, defaults to random string of 12 alphanumeric characters"),
	
	COMMAND_SYSTEM_REGISTERSERVER_DESCRIPTION("command.system.registerserver.description", "Register your current server with given friendly name."),
	COMMAND_SYSTEM_REGISTERSERVER_TIMEOUT("command.system.registerserver.timeout", "<red>Are you sure, you provided valid hashKey and server you want to register has up to date version of Portfel? He's not responding..."),
	COMMAND_SYSTEM_REGISTERSERVER_ALREADY("command.system.registerserver.already", "Is there any intelligent reason to register already registered server? Pro Tip: Check ID <aqua><underlined>{o}</underlined></aqua>."),
	COMMAND_SYSTEM_REGISTERSERVER_SUCCESS("command.system.registerserver.success", "<light_purple>You did it! You registered new portfel server with friendly name <aqua><underlined>{0}</underlined></aqua> and ID <aqua><underlined>{1}</underlined></aqua>!"),
	COMMAND_SYSTEM_REGISTERSERVER_ERROR("command.system.registerserver.error", "<dark_red>This... This was very interesting. Server returned an unknown response."),
	COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_NEEDED("command.system.registerserver.servername-needed", "We need a user friendly and memorable text for use as shorthand of server ID. Please provide id."),
	COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY("command.system.registerserver.servername-already", "Did you remember this shorthand is already in use for another server?"),
	//
	COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION("command.system.unregisterserver.description", "Unregister given server."),
	COMMAND_SYSTEM_UNREGISTERSERVER_SUCCESS("command.system.unregisterserver.success", "Successfully unregistered server with friendly name {0} and ID {1}."),
	//
	COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION("command.system.server.grant.description", "Grant selected server access to specified globar order."),
	COMMAND_SYSTEM_SERVER_GRANT_SUCCESS("command.system.server.grant.success", "Successfully granted {0} access to {1} global order."),
	COMMAND_SYSTEM_SERVER_GRANT_ALREADY("command.system.server.grant.already", "The same global order cannot be granted twice for the same server."),
	//
	COMMAND_SYSTEM_SERVER_REVOKE_DESCRIPTION("command.system.server.revoke.description", "Revoke selected server access to specified globar order."),
	COMMAND_SYSTEM_SERVER_REVOKE_SUCCESS("command.system.server.revoke.success", "Successfully revoked {0} access to {1} global order."),
	COMMAND_SYSTEM_SERVER_REVOKE_ALREADY("command.system.server.revoke.already", "To revoke an global order, you must first grant it."),
	//
	COMMAND_USER_DESCRIPTION("command.user.description", "User management main command."),
	//
	COMMAND_USER_INFO_DESCRIPTION("command.user.info.description", "Get extended info about user."),
	COMMAND_USER_INFO_HEADER("command.user.info.header", "User Info: {0}"),
	COMMAND_USER_INFO_UUID("command.user.info.uuid", "UUID: {0}"),
	COMMAND_USER_INFO_UUIDTYPE("command.user.info.uuidtype", "(type: {0})"),
	COMMAND_USER_INFO_STATUS("command.user.info.status", "Status: {0}"),
	COMMAND_USER_INFO_USERDATA("command.user.info.userdata", "Userdata:"),
	COMMAND_USER_INFO_BALANCE("command.user.info.balance", "Balance: {0}"),
	COMMAND_USER_INFO_MINORBALANCE("command.user.info.minorbalance", "Minor balance: {0}"),
	COMMAND_USER_INFO_INTOP("command.user.info.intop", "Can be in Top: {0}"),
	COMMAND_USER_INFO_SUGGEST("command.user.info.suggest", "Click to suggest command on chat"),
	COMMAND_USER_INFO_INSERT("command.user.info.insert", "Click+Shift to insert above text on chat"),
	//
	COMMAND_USER_ECO_DESCRIPTION("command.user.eco.description", "Manage user's economy."),
	//
	COMMAND_USER_ECO_SET_DESCRIPTION("command.user.eco.set.description", "Set user's balance to given amount."),
	COMMAND_USER_ECO_SET_SUCCESS("command.user.eco.set.success", "Set {1} as {0}'s balance."),
	//
	COMMAND_USER_ECO_GIVE_DESCRIPTION("command.user.eco.give.description", "Add given amount to user's balance."),
	COMMAND_USER_ECO_GIVE_SUCCESS("command.user.eco.give.success", "Add {1} to {0}'s balance."),
	//
	COMMAND_USER_ECO_TAKE_DESCRIPTION("command.user.eco.take.description", "Remove given amount from user's balance."),
	COMMAND_USER_ECO_TAKE_SUCCESS("command.user.eco.take.success", "Remove {1} from {0}'s balance."),
	COMMAND_USER_ECO_TAKE_SMALLER("command.user.eco.take.smaller", "User balance cannot be smaller than 0."),
	//
	COMMAND_USER_MINORECO_DESCRIPTION("command.user.minoreco.description", "Manage user's minor economy."),
	//
	COMMAND_USER_MINORECO_SET_DESCRIPTION("command.user.minoreco.set.description", "Set user's minor balance to given amount."),
	COMMAND_USER_MINORECO_SET_SUCCESS("command.user.minoreco.set.success", "Set {1} as {0}'s minor balance."),
	//
	COMMAND_USER_MINORECO_GIVE_DESCRIPTION("command.user.minoreco.give.description", "Add given amount to user's minor balance."),
	COMMAND_USER_MINORECO_GIVE_SUCCESS("command.user.minoreco.give.success", "Add {1} to {0}'s minor balance."),
	//
	COMMAND_USER_MINORECO_TAKE_DESCRIPTION("command.user.minoreco.take.description", "Remove given amount from user's minor balance."),
	COMMAND_USER_MINORECO_TAKE_SUCCESS("command.user.minoreco.take.success", "Remove {1} from {0}'s minor balance."),
	COMMAND_USER_MINORECO_TAKE_SMALLER("command.user.minoreco.take.smaller", "User minor balance cannot be smaller than 0."),
	//
	COMMAND_USER_TOP_DESCRIPTION("command.user.top.description", "Manage user's top position."),
	//
	COMMAND_USER_TOP_INFO_DESCRIPTION("command.user.top.info.description", "Get info about user's top."),
	COMMAND_USER_TOP_INFO_INTOP("command.user.top.info.intop", "{0}'s allowed in Top status:"),
	COMMAND_USER_TOP_INFO_POSITION("command.user.top.info.position", "{0}'s position:"),
	//
	COMMAND_USER_TOP_SET_DESCRIPTION("command.user.top.set.description", "Set wheter this user should by available in top."),
	COMMAND_USER_TOP_SET_SUCCESS("command.user.top.set.success", "Set {0}'s in top visibility to {1}."),
	COMMAND_USER_TOP_SET_ALREADY("command.user.top.set.already", "{0}'s in top visibility is already set to {1}."),
	//
	COMMAND_LOG_DESCRIPTION("command.log.description", "Log management main command."),
	//
	COMMAND_LOG_READ_DESCRIPTION("command.log.read.description", "Read logs."),
	COMMAND_LOG_READ_HEADER("command.log.read.header", "Showing last activities"),
	COMMAND_LOG_READ_PAGE("command.log.read.page", "page {0} of {1}"),
	COMMAND_LOG_READ_TIME_AGO("command.log.read.time-ago", "{0} ago"),
	//
	COMMAND_GIFTCODE_DESCRIPTION("command.giftcode.description", "Giftcode management main command."),
	//
	COMMAND_GIFTCODE_INFO_DESCRIPTION("command.giftcode.info.description", "Get extended info about giftcode."),
	COMMAND_GIFTCODE_INFO_HEADER("command.giftcode.info.header", "Giftcode Info: {0}"),
	COMMAND_GIFTCODE_INFO_CREATOR("command.giftcode.info.creator", "Creator: {0}"),
	COMMAND_GIFTCODE_INFO_UUID("command.giftcode.info.uuid", "UUID: {0}"),
	COMMAND_GIFTCODE_INFO_ORDER("command.giftcode.info.order", "Order: {0}"),
	COMMAND_GIFTCODE_INFO_ACCESSIBILITY("command.giftcode.info.accessibility", "Accessibility:"),
	COMMAND_GIFTCODE_INFO_ACCESSTYPE("command.giftcode.info.access-type", "Type: {0}"),
	COMMAND_GIFTCODE_INFO_ACCESSLIST("command.giftcode.info.access-list", "Allowed: {0}"),
	COMMAND_GIFTCODE_INFO_DATES("command.giftcode.info.dates", "Dates:"),
	COMMAND_GIFTCODE_INFO_CREATION("command.giftcode.info.creation", "Created: {0}"),
	COMMAND_GIFTCODE_INFO_EXPIRATION("command.giftcode.info.expiration", "Expires: {0}"),
	COMMAND_GIFTCODE_INFO_SUGGEST("command.giftcode.info.suggest", "Click to suggest command on chat"),
	COMMAND_GIFTCODE_INFO_INSERT("command.giftcode.info.intop", "Click+Shift to insert above text on chat"),
	//
	COMMAND_DELETEGIFTCODE_DESCRIPTION("command.deletegiftcode.description", "Delete giftcode."),
	COMMAND_DELETEGIFTCODE_SUCCESS("command.deletegiftcode.success", "Removed giftcode for {0}."),
	COMMAND_DELETEGIFTCODE_FAIL("command.deletegiftcode.fail", "Cannot delete giftcode for {0}."),
	//
	COMMAND_CREATEGIFTCODE_DESCRIPTION("command.creategiftcode.description", "Create giftcode."),
	COMMAND_CREATEGIFTCODE_SUCCESS("command.creategiftcode.success", "Created giftcode {0} for order {1} on {2} with expiration {3}."),
	COMMAND_CREATEGIFTCODE_FAIL("command.creategiftcode.fail", "Cannot create giftcode for {0}."),
	COMMAND_CREATEGIFTCODE_ALREADY("command.creategiftcode.already", "Gift code for {0} token already exists in database."),
	COMMAND_CREATEGIFTCODE_PAST("command.creategiftcode.past", "You're time traveller? The expiration date cannot be earlier than now."),
	//
	COMMAND_LISTGIFTCODES_DESCRIPTION("command.listgiftcodes.description", "List giftcodes."),
	COMMAND_LISTGIFTCODES_HEADER("command.listgiftcodes.header", "Showing giftcodes"),
	COMMAND_LISTGIFTCODES_PAGE("command.listgiftcodes.page", "page {0} of {1}"),
	COMMAND_LISTGIFTCODES_LIFETIME("command.listgiftcodes.lifetime", "lifetime"),
	COMMAND_LISTGIFTCODES_TIME_AGO("command.listgiftcodes.time-ago", "{0} ago"),
	COMMAND_LISTGIFTCODES_SUGGEST("command.listgiftcodes.suggest", "Click to insert displayname on chat"),
	COMMAND_LISTGIFTCODES_INSERT("command.listgiftcodes.insert", "Click+Shift to insert unique ID on chat"),
	COMMAND_LISTGIFTCODES_EXPIRATION("command.listgiftcodes.expiration", "Expiration: {0}"),
	
	COMMAND_VALUENAMES_SERVERID("command.value-names.server-id", "server ID"),
	COMMAND_VALUENAMES_SERVERFRIENDLYNAME("command.value-names.server-friendly-name", "server friendly name"),
	
	TOKEN_CHECK_USAGE("token.check.usage", "Correct usage: /{0} <token>"),
	TOKEN_CHECK_ALREADY("token.check.already", "<red>Why are you spamming me? Wait for the result of previus check."),
	TOKEN_CHECK_FULLPOOL("token.check.full-pool", "<red>So many players to check, so few resources to do this. Please wait, the pool is full."),
	TOKEN_CHECK_INVALID("token.check.invalid", "Probably you provided an inexistient token. Prove? It doesn't exist!"),
	TOKEN_CHECK_SERVER_INVALID_REGISTERED("token.check.server.invalid.registered", "<red>This game mode doesn't support tokens. Please try another game mode, or just throw it away..."),
	TOKEN_CHECK_SERVER_INVALID_WHITELIST("token.check.server.invalid.whitelist", "<red>This token doesn't like this game mode. But it should like: {0}"),
	TOKEN_CHECK_SERVER_INVALID_WHITELIST_SERVER_FORMAT("token.check.server.invalid.whitelist.server-format", "<aqua>{0}"),
	
	LOG_PREFIX("log.prefix", "LOG"),
	LOG_SUGGEST("log.suggest", "Click to insert displayname on chat"),
	LOG_INSERT("log.insert", "Click+Shift to insert unique ID on chat"),
	LOG_VALUE_ACTION("log.value.action", "Action: {0}"),
	LOG_VALUE_OLD_BALANCE("log.value.old-balance", "Old balance: {0}"),
	LOG_VALUE_DATE("log.value.date", "Date: {0}"),
	
	MAIN_VALUENAME_DESCRIPTION("main.value-name.description", "Description:"),
	MAIN_VALUENAME_ALIASES("main.value-name.aliases", "Aliases:"),
	MAIN_VALUENAME_ENABLED("main.value-name.enabled", "Enabled:"),
	MAIN_VALUENAME_AUTHORS("main.value-name.aliases", "Authors:"),
	MAIN_VALUENAME_PERMISSION("main.value-name.permission", "Permission:"),
	
	MAIN_VALUE_TRUE("main.value.true", "true"),
	MAIN_VALUE_FALSE("main.value.false", "false"),
	MAIN_VALUE_YES("main.value.yes", "yes"),
	MAIN_VALUE_NO("main.value.no", "no"),
	MAIN_VALUE_ONLINE("main.value.online", "Online"),
	MAIN_VALUE_OFFLINE("main.value.offline", "Offline"),
	
	MAIN_MESSAGE_INSERTION("main.message.insertion", "<hover:show_text:\"<dark_aqua>» <aqua>Click to insert the {1}.\"><click:suggest_command:{0}><insert:{0}>{0}</insert></click></hover>"),
	
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
	private ProxyLangKey(String path, String defString) {
		this(path, defString, true);
	}
	
	/**
	 * Lang enum constructor.
	 * 
	 * @param path The string path.
	 * @param start The default string.
	 * @param whether value should be loaded from file
	 */
	private ProxyLangKey(@NotNull String path, @NotNull String defString, boolean modifiable) {
		this.path = path;
		this.defString = defString;
		this.modifiable = modifiable;
	}

}
