package me.szumielxd.portfel.proxy.commands.system.server;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.managers.OrdersManager.GlobalOrder;

public class GrantOrderCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.ORDER);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION;
	
	
	public GrantOrderCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "grant");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		parseArguments(sender, parsedContext).ifPresent(parsed -> {
			GlobalOrder order = (GlobalOrder) parsed.parsedStaticArg(0);
			UUID server = (UUID) parsedContext.parsedStaticArg(0);
			AccessManager access = ((PortfelProxyImpl<C>)this.getPlugin()).getAccessManager();
			if (access.canAccess(server, order.getName())) {
				ProxyLangKey.COMMAND_SYSTEM_SERVER_GRANT_ALREADY.draft()
						.sendPrefixed(sender);
			} else {
				access.giveAccess(server, order.getName());
				ProxyLangKey.COMMAND_SYSTEM_SERVER_GRANT_SUCCESS
						.draft(access.getServerNames().get(server), order.getName())
						.sendPrefixed(sender);
			}
		});
	}

}
