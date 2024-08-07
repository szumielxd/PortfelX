package me.szumielxd.portfel.proxy.commands;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import lombok.experimental.UtilityClass;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.managers.OrdersManager.GlobalOrder;
import me.szumielxd.portfel.proxy.objects.PrizeToken;

@UtilityClass
public class CommonArgs {
	
	
	private static PortfelProxyImpl<?> plugin;
	public static final List<String> NUMBERS_LIST = Arrays.asList("1", "2", "5", "10", "20", "50", "100");
	
	
	
	
	
	public static <C> void init(PortfelProxyImpl<C> plugin) {
		CommonArgs.plugin = plugin;
	}
	
	
	public static final CmdArg SERVER = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_SERVER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_SERVER_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_SERVER_ERROR, s -> {
			AccessManager access = plugin.getAccessManager();
			try {
				UUID uuid = UUID.fromString(s);
				if (access.canAccess(uuid)) {
					return uuid;
				}
			} catch (IllegalArgumentException e) {
				// fallback
			}
			return access.getServerByName(s);
	},
	s -> List.copyOf(plugin.getAccessManager().getServerNames().values()));
	
	//
	
	public static final CmdArg USER = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_USER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_USER_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_USER_ERROR, s -> {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(s);
			} catch (IllegalArgumentException e) {
				// not correct uuid
			}
			try {
				if (uuid != null) {
					return plugin.getUserManager().getOrLoadUser(uuid);
				} else {
					return plugin.getUserManager().getOrLoadUser(s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
	},
	s -> plugin.getCommonServer().getPlayers().stream()
			.map(ProxyPlayer::getName)
			.toList());
	
	//
	
	public static final CmdArg TOKEN = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_TOKEN_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKEN_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_TOKEN_ERROR, s -> {
		try {
			return plugin.getTokenDatabase().getToken(s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	},
	s -> plugin.getTokenManager().getCachedTokens().stream()
			.map(PrizeToken::getToken)
			.toList());
	
	//
	
	public static final CmdArg ORDER = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_ORDER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_ORDER_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_ORDER_ERROR,
			s -> plugin.getOrdersManager().getOrders().get(s.toLowerCase()),
			s -> plugin.getOrdersManager().getOrders().values().stream()
					.map(GlobalOrder::getName)
					.toList());
	
	//
	
	public static final CmdArg ECO_AMOUNT = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_ECO_AMOUNT_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_ECO_AMOUNT_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_ECO_AMOUNT_ERROR, s -> {
		try {
			long val = Long.parseLong(s);
			return val < 0 ? null : val;
		} catch (NumberFormatException e) {
			return null;
		}
	},
	s -> Arrays.asList("0", "1", "2", "5", "10", "20", "50", "100", "200", "500", "1000"));

	//
	
	public static final CmdArg REASON = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_REASON_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_REASON_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_REASON_ERROR, s -> s,
	s -> Arrays.asList("Event", "Punishment", "Rollback", "\"BecauseIWant\"", "ErrorFix", "Test"));

	//
	
	public static final CmdArg INTOP = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_INTOP_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_INTOP_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_INTOP_ERROR, s -> {
		if ("true".equalsIgnoreCase(s)) return true;
		if ("false".equalsIgnoreCase(s)) return false;
		return null;
	},
	s -> Arrays.asList("true", "false"));
	
	//
	
	public static final CmdArg PAGENUMBER = new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_PAGENUMBER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_PAGENUMBER_DESCRIPTION, null, CommonArgs::tryParseUnsignedNotZeroInt,
			(s, arr) -> NUMBERS_LIST);
	
	//
	
	public static final CmdArg PAGESIZE = new CmdArg(true, "size=", ProxyLangKey.COMMAND_ARGTYPES_PAGESIZE_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_PAGESIZE_DESCRIPTION, null, CommonArgs::tryParseUnsignedNotZeroInt,
			(s, arr) -> NUMBERS_LIST.stream()
					.map("size="::concat)
					.toList());
	
	
	
	
	
	private static @Nullable Integer tryParseUnsignedNotZeroInt(String text) {
		try {
			int i = Integer.parseInt(text);
			return i > 0 ? i : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
}
