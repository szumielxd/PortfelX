package me.szumielxd.portfel.proxy.commands.system;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class RegisterServerCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(
			// serverName
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_SERVERNAME_DISPLAY,
					ProxyLangKey.COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION,
					null, s -> s, s -> List.of()),
			// hashKey
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_HASHKEY_DISPLAY,
					ProxyLangKey.COMMAND_ARGTYPES_HASHKEY_DESCRIPTION,
					null, s -> s, s -> List.of()));
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_DESCRIPTION;
	@Getter private final @NotNull CommandAccess access = CommandAccess.PLAYERS;
	
	
	public RegisterServerCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "registerserver", "createserver");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		PortfelProxyImpl<C> pl = (PortfelProxyImpl<C>)this.getPlugin();
		String serverName = parsedContext.argLeft(0);
		String serverHashKey = parsedContext.argLeft(1);
		if (pl.getAccessManager().getServerByName(serverName) != null) {
			ProxyLangKey.COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY
					.draft()
					.sendPrefixed(sender);
		} else {
			pl.getAccessManager().getRegistrationManager().requestRegistration((ProxyPlayer<C>) sender, serverName, serverHashKey);
		}
	}

}
