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
	COMMAND_SYSTEM_REGISTERSERVER_ALREADY("command.system.registerserver.already", "Is there any intelligent reason to register already registered server? Pro Tip: Check ID <aqua><underlined>{0}</underlined></aqua>."),
	COMMAND_SYSTEM_REGISTERSERVER_SUCCESS("command.system.registerserver.success", "<light_purple>You did it! You registered new portfel server with friendly name <aqua><underlined>{0}</underlined></aqua> and ID <aqua><underlined>{1}</underlined></aqua>!"),
	COMMAND_SYSTEM_REGISTERSERVER_ERROR("command.system.registerserver.error", "<dark_red>This... This was very interesting. Server returned an unknown response."),
	COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_NEEDED("command.system.registerserver.servername-needed", "We need a user friendly and memorable text for use as shorthand of server ID. Please provide id."),
	COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY("command.system.registerserver.servername-already", "<red>Did you remember this shorthand is already in use for another server?"),
	//
	COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION("command.system.unregisterserver.description", "Unregister given server."),
	COMMAND_SYSTEM_UNREGISTERSERVER_SUCCESS("command.system.unregisterserver.success", "<light_purple>Successfully unregistered server with friendly name <light_purple>{0}</light_purple> and ID <light_purple>{1}</light_purple>."),
	//
	COMMAND_SYSTEM_SERVER_DESCRIPTION("command.system.server.description", "Server management main command."),
	//
	COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION("command.system.server.grant.description", "Grant selected server access to specified globar order."),
	COMMAND_SYSTEM_SERVER_GRANT_SUCCESS("command.system.server.grant.success", "<light_purple>Successfully granted <aqua>{0}</aqua> access to <aqua>{1}</aqua> global order."),
	COMMAND_SYSTEM_SERVER_GRANT_ALREADY("command.system.server.grant.already", "<red>The same global order cannot be granted twice for the same server."),
	//
	COMMAND_SYSTEM_SERVER_REVOKE_DESCRIPTION("command.system.server.revoke.description", "Revoke selected server access to specified globar order."),
	COMMAND_SYSTEM_SERVER_REVOKE_SUCCESS("command.system.server.revoke.success", "<light_purple>Successfully revoked <aqua>{0}</aqua> access to <aqua>{1}</aqua> global order."),
	COMMAND_SYSTEM_SERVER_REVOKE_ALREADY("command.system.server.revoke.already", "<red>To revoke an global order, you must first grant it."),
	//
	COMMAND_USER_DESCRIPTION("command.user.description", "User management main command."),
	//
	COMMAND_USER_INFO_DESCRIPTION("command.user.info.description", "Get extended info about user."),
	COMMAND_USER_INFO_HEADER("command.user.info.header", "<light_purple><bold>> </bold><dark_purple>User Info: {0}"),
	COMMAND_USER_INFO_UUID("command.user.info.uuid", "<white>- <light_purple>UUID: {0}"),
	COMMAND_USER_INFO_UUIDTYPE("command.user.info.uuidtype", "<gray>   (type: {0})"),
	COMMAND_USER_INFO_STATUS("command.user.info.status", "<white>- <dark_purple>Status: {0}"),
	COMMAND_USER_INFO_USERDATA("command.user.info.userdata", "<white>- <dark_purple>Userdata:"),
	COMMAND_USER_INFO_BALANCE("command.user.info.balance", "   <command:suggest_command:/{1}><hover:show_text:\\\"Click to insert balance command\\\"><dark_purple>Balance: <aqua>{0}"),
	COMMAND_USER_INFO_MINORBALANCE("command.user.info.minorbalance", "   <command:suggest_command:/{1}><hover:show_text:\\\"Click to insert minor balance command\\\"><dark_purple>Minor balance: <aqua>{0}"),
	COMMAND_USER_INFO_INTOP("command.user.info.intop", "   <dark_purple>Can be in Top: {0}"),
	COMMAND_USER_INFO_VALUE("command.user.info.value", "<click:suggest_command:/{1} {0}><insert:{0}><hover:show_text:\\\"{2}\\\"><white>{0}"),
	COMMAND_USER_INFO_VALUEHOVER("command.user.info.value-hover", "<dark_gray>» <gray>Click to suggest command on chat</gray>\\n» Click+Shift to insert above text on chat"),
	//
	COMMAND_USER_ECO_DESCRIPTION("command.user.eco.description", "Manage user's economy."),
	//
	COMMAND_USER_ECO_SET_DESCRIPTION("command.user.eco.set.description", "Set user's balance to given amount."),
	COMMAND_USER_ECO_SET_SUCCESS("command.user.eco.set.success", "<light_purple>Set <aqua>{1}</aqua> as <aqua>{0}</aqua>'s balance."),
	//
	COMMAND_USER_ECO_GIVE_DESCRIPTION("command.user.eco.give.description", "Add given amount to user's balance."),
	COMMAND_USER_ECO_GIVE_SUCCESS("command.user.eco.give.success", "<light_purple>Add <aqua>{1}</aqua> to <aqua>{0}</aqua>'s balance."),
	//
	COMMAND_USER_ECO_TAKE_DESCRIPTION("command.user.eco.take.description", "Remove given amount from user's balance."),
	COMMAND_USER_ECO_TAKE_SUCCESS("command.user.eco.take.success", "<light_purple>Remove <aqua>{1}</aqua> from <aqua>{0}</aqua>'s balance."),
	COMMAND_USER_ECO_TAKE_SMALLER("command.user.eco.take.smaller", "<red>User balance cannot be smaller than 0."),
	//
	COMMAND_USER_MINORECO_DESCRIPTION("command.user.minoreco.description", "Manage user's minor economy."),
	//
	COMMAND_USER_MINORECO_SET_DESCRIPTION("command.user.minoreco.set.description", "Set user's minor balance to given amount."),
	COMMAND_USER_MINORECO_SET_SUCCESS("command.user.minoreco.set.success", "<light_purple>Set <aqua>{1}</aqua> as <aqua>{0}</aqua>'s minor balance."),
	//
	COMMAND_USER_MINORECO_GIVE_DESCRIPTION("command.user.minoreco.give.description", "Add given amount to user's minor balance."),
	COMMAND_USER_MINORECO_GIVE_SUCCESS("command.user.minoreco.give.success", "<light_purple>Add <aqua>{1}</aqua> to <aqua>{0}</aqua>'s minor balance."),
	//
	COMMAND_USER_MINORECO_TAKE_DESCRIPTION("command.user.minoreco.take.description", "Remove given amount from user's minor balance."),
	COMMAND_USER_MINORECO_TAKE_SUCCESS("command.user.minoreco.take.success", "<light_purple>Remove <aqua>{1}</aqua> from <aqua>{0}</aqua>'s minor balance."),
	COMMAND_USER_MINORECO_TAKE_SMALLER("command.user.minoreco.take.smaller", "<red>User minor balance cannot be smaller than 0."),
	//
	COMMAND_USER_TOP_DESCRIPTION("command.user.top.description", "Manage user's top position."),
	//
	COMMAND_USER_TOP_INFO_DESCRIPTION("command.user.top.info.description", "Get info about user's top."),
	COMMAND_USER_TOP_INFO_INTOP("command.user.top.info.intop", "<dark_purple>{0}'s allowed in Top status:"),
	COMMAND_USER_TOP_INFO_INTOPVALUE("command.user.top.info.intop-value", "<light_purple>-> {0}"),
	COMMAND_USER_TOP_INFO_POSITION("command.user.top.info.position", "<dark_purple>{0}'s position:"),
	COMMAND_USER_TOP_INFO_POSITIONVALUE("command.user.top.info.position-value", "<light_purple>-> <aqua>{0}"),
	//
	COMMAND_USER_TOP_SET_DESCRIPTION("command.user.top.set.description", "Set wheter this user should by available in top."),
	COMMAND_USER_TOP_SET_SUCCESS("command.user.top.set.success", "<light_purple>Set <aqua>{0}</aqua>'s in top visibility to <aqua>{1}</aqua>."),
	COMMAND_USER_TOP_SET_ALREADY("command.user.top.set.already", "<red><dark_red>{0}</dark_red>'s in top visibility is already set to <dark_red>{1}</dark_red>."),
	//
	COMMAND_LOG_DESCRIPTION("command.log.description", "Log management main command."),
	//
	COMMAND_LOG_READ_DESCRIPTION("command.log.read.description", "Read logs."),
	COMMAND_LOG_READ_HEADER("command.log.read.header", "<dark_purple>Showing last activities <gray>(page <white>{0}</white> of <white>{1}</white>)"),
	COMMAND_LOG_READ_LINE1("command.log.read.line1", "<light_purple>#{0} <dark_gray>(<gray>{1} ago</gray>) {2}"),
	//
	COMMAND_GIFTCODE_DESCRIPTION("command.giftcode.description", "Giftcode management main command."),
	//
	
	ACOMMAND_USER_INFO_HEADER("command.user.info.header", "<light_purple><bold>> </bold><dark_purple>User Info: {0}"),
	ACOMMAND_USER_INFO_UUID("command.user.info.uuid", "<white>- <light_purple>UUID: {0}"),
	ACOMMAND_USER_INFO_UUIDTYPE("command.user.info.uuidtype", "<gray>   (type: {0})"),
	ACOMMAND_USER_INFO_STATUS("command.user.info.status", "<white>- <dark_purple>Status: {0}"),
	ACOMMAND_USER_INFO_USERDATA("command.user.info.userdata", "<white>- <dark_purple>Userdata:"),
	ACOMMAND_USER_INFO_BALANCE("command.user.info.balance", "   <command:suggest_command:/{1}><hover:show_text:\\\"Click to insert balance command\\\"><dark_purple>Balance: <aqua>{0}"),
	ACOMMAND_USER_INFO_MINORBALANCE("command.user.info.minorbalance", "   <command:suggest_command:/{1}><hover:show_text:\\\"Click to insert minor balance command\\\"><dark_purple>Minor balance: <aqua>{0}"),
	ACOMMAND_USER_INFO_INTOP("command.user.info.intop", "   <dark_purple>Can be in Top: {0}"),
	ACOMMAND_USER_INFO_VALUE("command.user.info.value", "<click:suggest_command:/{1} {0}><insert:{0}><hover:show_text:\\\"{2}\\\"><white>{0}"),
	ACOMMAND_USER_INFO_VALUEHOVER("command.user.info.value-hover", "<dark_gray>» <gray>Click to suggest command on chat</gray>\\n» Click+Shift to insert above text on chat"),
	
	
	//
	COMMAND_GIFTCODE_INFO_DESCRIPTION("command.giftcode.info.description", "Get extended info about giftcode."),
	COMMAND_GIFTCODE_INFO_HEADER("command.giftcode.info.header", "<light_purple><bold>> </bold><dark_purple>Giftcode Info: {0}"),
	COMMAND_GIFTCODE_INFO_CREATOR("command.giftcode.info.creator", "<white>- <light_purple>Creator: {0}"),
	COMMAND_GIFTCODE_INFO_UUID("command.giftcode.info.uuid", "   <dark_purple>UUID: {0}"),
	COMMAND_GIFTCODE_INFO_ORDER("command.giftcode.info.order", "<white>- <dark_purple>Order: <aqua>{0}"),
	COMMAND_GIFTCODE_INFO_ACCESSIBILITY("command.giftcode.info.accessibility", "<white>- <light_purple>Accessibility:"),
	COMMAND_GIFTCODE_INFO_ACCESSTYPE("command.giftcode.info.access-type", "   <dark_purple>Type: <aqua>{0}"),
	COMMAND_GIFTCODE_INFO_ACCESSLIST("command.giftcode.info.access-list", "   <dark_purple>Allowed: <gray>{0}"),
	COMMAND_GIFTCODE_INFO_ACCESSLIST_FORMAT("command.giftcode.info.access-list.format", "<aqua>{0}"),
	COMMAND_GIFTCODE_INFO_DATES("command.giftcode.info.dates", "<white>- <light_purple>Dates:"),
	COMMAND_GIFTCODE_INFO_CREATION("command.giftcode.info.creation", "   <dark_purple>Created: <aqua>{0}"),
	COMMAND_GIFTCODE_INFO_EXPIRATION("command.giftcode.info.expiration", "   <dark_purple>Expires: <aqua>{0}"),
	COMMAND_GIFTCODE_INFO_SUGGEST("command.giftcode.info.suggest", "Click to suggest command on chat"),
	COMMAND_GIFTCODE_INFO_INSERT("command.giftcode.info.intop", "Click+Shift to insert above text on chat"),
	COMMAND_GIFTCODE_INFO_VALUE("command.giftcode.info.value", "<click:suggest_command:/{1} {0}><insert:{0}><hover:show_text:\\\"{2}\\\"><white>{0}"),
	COMMAND_GIFTCODE_INFO_VALUEHOVER("command.giftcode.info.value-hover", "<dark_gray>» <gray>Click to suggest command on chat</gray>\\n» Click+Shift to insert above text on chat"),
	//
	COMMAND_DELETEGIFTCODE_DESCRIPTION("command.deletegiftcode.description", "Delete giftcode."),
	COMMAND_DELETEGIFTCODE_SUCCESS("command.deletegiftcode.success", "<green>Removed giftcode for <aqua>{0}</aqua>."),
	COMMAND_DELETEGIFTCODE_FAIL("command.deletegiftcode.fail", "<red>Cannot delete giftcode for <aqua>{0}</aqua>."),
	//
	COMMAND_CREATEGIFTCODE_DESCRIPTION("command.creategiftcode.description", "Create giftcode."),
	COMMAND_CREATEGIFTCODE_SUCCESS("command.creategiftcode.success", "<green>Created giftcode <aqua>{0}</aqua> for order <aqua>{1}</aqua> on <aqua>{2}</aqua> with expiration <aqua>{3}</aqua>."),
	COMMAND_CREATEGIFTCODE_FAIL("command.creategiftcode.fail", "<red>Cannot create giftcode for <aqua>{0}</aqua>."),
	COMMAND_CREATEGIFTCODE_ALREADY("command.creategiftcode.already", "<red>Gift code for <aqua>{0}</aqua> token already exists in database."),
	COMMAND_CREATEGIFTCODE_PAST("command.creategiftcode.past", "<red>You're time traveller? The expiration date cannot be earlier than now."),
	//
	COMMAND_LISTGIFTCODES_DESCRIPTION("command.listgiftcodes.description", "List giftcodes."),
	COMMAND_LISTGIFTCODES_HEADER("command.listgiftcodes.header", "<dark_purple>Showing giftcodes <gray>(page <white>{0}</white> of <white>{1}</white>)"),
	COMMAND_LISTGIFTCODES_ENTRY_FIRSTLINE("command.listgiftcodes.entry.first-line", "<hover:show_text:\\\"{0}\\\"><insert:{1}><light_purple>{1} <dark_gray>(<gray>{2}</gray>) ({3}) [{4}]"), // id, time_ago, executor, expiration_indicator
	COMMAND_LISTGIFTCODES_ENTRY_SECONDLINE("command.listgiftcodes.entry.second-line", "<hover:show_text:\"{0}\"><insert:{1}><gray>> <white>{1} {2}"), // order, servers
	COMMAND_LISTGIFTCODES_LIFETIME("command.listgiftcodes.lifetime", "<red>lifetime"),
	COMMAND_LISTGIFTCODES_ENDTIME("command.listgiftcodes.end-time", "<aqua>✝ {0}"),
	COMMAND_LISTGIFTCODES_TIME_AGO("command.listgiftcodes.time-ago", "{0} ago"),
	COMMAND_LISTGIFTCODES_EXPIRATION("command.listgiftcodes.expiration", "<gray>Expiration: {0}"),
	COMMAND_LISTGIFTCODES_EXPIRATION_EXPIRABLE("command.listgiftcodes.expiration.expirable", "<aqua>E"),
	COMMAND_LISTGIFTCODES_EXPIRATION_EXPIRED("command.listgiftcodes.expiration.expired", "<gray>✝"),
	COMMAND_LISTGIFTCODES_EXPIRATION_LIFETIME("command.listgiftcodes.expiration.lifetime", "<gold>L"),
	COMMAND_LISTGIFTCODES_SERVERSELECTOR_ANY("command.listgiftcodes.server-selector.any", "<aqua>ANY"),
	COMMAND_LISTGIFTCODES_SERVERSELECTOR_REGISTERED("command.listgiftcodes.server-selector.registered", "<aqua>REGISTERED"),
	COMMAND_LISTGIFTCODES_SERVERSELECTOR_WHITELIST("command.listgiftcodes.server-selector.whitelist", "<aqua>WHITELIST <dark_gray>[{0}]"),
	COMMAND_LISTGIFTCODES_SERVERSELECTOR_WHITELIST_ENTRY("command.listgiftcodes.server-selector.whitelist.entry", "<gold>{0}"),
	COMMAND_LISTGIFTCODES_USER("command.listgiftcodes.user", "<hover:show_text:\"<aqua>{1}\n<dark_gray>» <gray>Click to insert displayname on chat</gray>\n» <gray>Click+Shift to insert unique ID on chat\">click:suggest_command:{0}><insert:{1}><green>{0}"),
	
	COMMAND_VALUENAMES_SERVERID("command.value-names.server-id", "server ID"),
	COMMAND_VALUENAMES_SERVERFRIENDLYNAME("command.value-names.server-friendly-name", "server friendly name"),
	
	TOKEN_CHECK_USAGE("token.check.usage", "<red>Correct usage: /{0} <token>"),
	TOKEN_CHECK_ALREADY("token.check.already", "<red>Why are you spamming me? Wait for the result of previus check."),
	TOKEN_CHECK_FULLPOOL("token.check.full-pool", "<red>So many players to check, so few resources to do this. Please wait, the pool is full."),
	TOKEN_CHECK_INVALID("token.check.invalid", "Probably you provided an inexistient token. Prove? It doesn't exist!"),
	TOKEN_CHECK_SERVER_INVALID_REGISTERED("token.check.server.invalid.registered", "<red>This game mode doesn't support tokens. Please try another game mode, or just throw it away..."),
	TOKEN_CHECK_SERVER_INVALID_WHITELIST("token.check.server.invalid.whitelist", "<red>This token doesn't like this game mode. But it should like: {0}"),
	TOKEN_CHECK_SERVER_INVALID_WHITELIST_SERVER_FORMAT("token.check.server.invalid.whitelist.server-format", "<aqua>{0}"),
	
	LOG_PREFIX("log.prefix", "<dark_aqua>LOG "),
	LOG_MESSAGE_LINE1("log.message.line1", "<gray>> <dark_gray>(<green>{0}</green>) [<aqua>{1}</aqua>]"),
	LOG_MESSAGE_LINE2("log.message.line2", "<gray>> <white>{0} {1}"),
	LOG_USER_INTERACTIVE("log.user.interactive", "<hover:show_text:\"<aqua>{2}\n<dark_gray>» <gray>Click to insert displayname on chat</gray>\n» <gray>Click+Shift to insert unique ID on chat\"><click:suggest_command:{1}><insert:{2}>{0}"),
	LOG_ACTION_AMOUNT("log.action.amount", "<hover:show_text:\"{0}\n<gray>Action: <aqua>{1}</aqua>\nOld balance: <aqua>{2}\">{0}"),
	LOG_ACTION_NAME("log.action.name", "<white><hover:show_text:\"<white>{0}<gray>Date: <aqua>{1}\">{0}"),
	
	SERVER_FIELD_FRIENDLYNAME("server.field.friendly-name", "server friendly name"),
	SERVER_FIELD_ID("server.field.id", "server ID"),
	
	MAIN_VALUENAME_DESCRIPTION("main.value-name.description", "Description:"),
	MAIN_VALUENAME_ALIASES("main.value-name.aliases", "Aliases:"),
	MAIN_VALUENAME_ENABLED("main.value-name.enabled", "Enabled:"),
	MAIN_VALUENAME_AUTHORS("main.value-name.aliases", "Authors:"),
	MAIN_VALUENAME_PERMISSION("main.value-name.permission", "Permission:"),
	
	MAIN_VALUE_YES("main.value.yes", "yes"),
	MAIN_VALUE_NO("main.value.no", "no"),
	
	MAIN_MESSAGE_INSERTION("main.message.insertion", "<hover:show_text:\"<dark_aqua>» <aqua>Click to insert {1} on chat.\"><click:suggest_command:{0}><insert:{0}>{0}</insert></click></hover>"),
	
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
