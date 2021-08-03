package me.szumielxd.portfel.bungee.commands;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.managers.AccessManager;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommonArgs {
	
	
	private static PortfelBungee plugin;
	
	
	public static void init(PortfelBungee plugin) {
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

}
