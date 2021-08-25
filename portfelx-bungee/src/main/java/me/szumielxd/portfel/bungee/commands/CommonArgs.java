package me.szumielxd.portfel.bungee.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.managers.AccessManager;
import me.szumielxd.portfel.bungee.managers.OrdersManager.GlobalOrder;
import me.szumielxd.portfel.bungee.objects.PrizeToken;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommonArgs {
	
	
	private static PortfelBungeeImpl plugin;
	public static List<String> NUMBERS_LIST = Arrays.asList("1", "2", "5", "10", "20", "50", "100");
	
	
	public static void init(PortfelBungeeImpl plugin) {
		CommonArgs.plugin = plugin;
	}
	
	
	public static final CmdArg SERVER = new CmdArg(LangKey.COMMAND_ARGTYPES_SERVER_DISPLAY, LangKey.COMMAND_ARGTYPES_SERVER_DESCRIPTION, LangKey.COMMAND_ARGTYPES_SERVER_ERROR, s -> {
			AccessManager access = plugin.getAccessManager();
			try {
				UUID uuid = UUID.fromString(s);
				if (access.canAccess(uuid)) return uuid;
			} catch (IllegalArgumentException e) {}
			return access.getServerByName(s);
	},
	s -> new ArrayList<>(plugin.getAccessManager().getServerNames().values()));
	
	//
	
	public static final CmdArg USER = new CmdArg(LangKey.COMMAND_ARGTYPES_USER_DISPLAY, LangKey.COMMAND_ARGTYPES_USER_DESCRIPTION, LangKey.COMMAND_ARGTYPES_USER_ERROR, s -> {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(s);
				try {
					if (uuid != null) return plugin.getUserManager().getOrLoadUser(uuid);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				try {
					return plugin.getUserManager().getOrLoadUser(s);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return null;
	},
	s -> plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()));
	
	//
	
	public static final CmdArg TOKEN = new CmdArg(LangKey.COMMAND_ARGTYPES_TOKEN_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKEN_DESCRIPTION, LangKey.COMMAND_ARGTYPES_TOKEN_ERROR, s -> {
		try {
			return plugin.getTokenDB().getToken(s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	},
	s -> plugin.getTokenManager().getCachedTokens().stream().map(PrizeToken::getToken).collect(Collectors.toList()));
	
	//
	
	public static final CmdArg ORDER = new CmdArg(LangKey.COMMAND_ARGTYPES_ORDER_DISPLAY, LangKey.COMMAND_ARGTYPES_ORDER_DESCRIPTION, LangKey.COMMAND_ARGTYPES_ORDER_ERROR, s -> {
			return plugin.getOrdersManager().getOrders().get(s.toLowerCase());
	},
	s -> plugin.getOrdersManager().getOrders().values().stream().map(GlobalOrder::getName).collect(Collectors.toList()));
	
	//
	
	public static final CmdArg ECO_AMOUNT = new CmdArg(LangKey.COMMAND_ARGTYPES_ECO_AMOUNT_DISPLAY, LangKey.COMMAND_ARGTYPES_ECO_AMOUNT_DESCRIPTION, LangKey.COMMAND_ARGTYPES_ECO_AMOUNT_ERROR, s -> {
		try {
			long val = Long.parseLong(s);
			return val < 0 ? null : val;
		} catch (NumberFormatException e) {
			return null;
		}
	},
	s -> Arrays.asList("0", "1", "2", "5", "10", "20", "50", "100", "200", "500", "1000"));

	//
	
	public static final CmdArg REASON = new CmdArg(LangKey.COMMAND_ARGTYPES_REASON_DISPLAY, LangKey.COMMAND_ARGTYPES_REASON_DESCRIPTION, LangKey.COMMAND_ARGTYPES_REASON_ERROR, s -> s,
	s -> Arrays.asList("Event", "Punishment", "Rollback", "\"BecauseIWant\"", "ErrorFix", "Test"));

	//
	
	public static final CmdArg INTOP = new CmdArg(LangKey.COMMAND_ARGTYPES_INTOP_DISPLAY, LangKey.COMMAND_ARGTYPES_INTOP_DESCRIPTION, LangKey.COMMAND_ARGTYPES_INTOP_ERROR, s -> {
		if ("true".equalsIgnoreCase(s)) return true;
		if ("false".equalsIgnoreCase(s)) return false;
		return null;
	},
	s -> Arrays.asList("true", "false"));
	
	//
	
	public static final CmdArg PAGENUMBER = new CmdArg(LangKey.COMMAND_ARGTYPES_PAGENUMBER_DISPLAY, LangKey.COMMAND_ARGTYPES_PAGENUMBER_DESCRIPTION, null, CommonArgs::tryParseUnsignedNotZeroInt, (s, arr) -> {
		return NUMBERS_LIST;
	});
	
	//
	
	public static final CmdArg PAGESIZE = new CmdArg(true, "size=", LangKey.COMMAND_ARGTYPES_PAGESIZE_DISPLAY, LangKey.COMMAND_ARGTYPES_PAGESIZE_DESCRIPTION, null, CommonArgs::tryParseUnsignedNotZeroInt, (s, arr) -> {
		return NUMBERS_LIST.stream().map(n -> "size="+n).collect(Collectors.toList());
	});
	
	
	
	
	
	private static @Nullable Integer tryParseUnsignedNotZeroInt(String text) {
		try {
			int i = Integer.parseInt(text);
			return i > 0 ? i : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
}
