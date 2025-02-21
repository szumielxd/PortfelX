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

public class RevokeOrderCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(
			new CmdArg(
					ProxyLangKey.COMMAND_ARGTYPES_ORDER_DISPLAY,
					ProxyLangKey.COMMAND_ARGTYPES_ORDER_DESCRIPTION,
					null, s -> s.toLowerCase(),
					(s, label) -> {
						UUID server = (UUID) CommonArgs.SERVER.parseArg(label[3]);
						if (server == null) return List.of();
						return ((PortfelProxyImpl<C>) getPlugin()).getAccessManager().getAllowedOrders(server);
					}));
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_SYSTEM_SERVER_REVOKE_DESCRIPTION;

	
	public RevokeOrderCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "revoke");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		this.parseArguments(sender, parsedContext).ifPresent(parsed -> {
			String order = (String) parsed.parsedStaticArg(0);
			UUID server = (UUID) parsedContext.parsedStaticArg(0);
			AccessManager access = ((PortfelProxyImpl<C>) this.getPlugin()).getAccessManager();
			if (!access.canAccess(server, order)) {
				ProxyLangKey.COMMAND_SYSTEM_SERVER_REVOKE_ALREADY.draft()
						.sendPrefixed(sender);
			} else {
				access.takeAccess(server, order);
				ProxyLangKey.COMMAND_SYSTEM_SERVER_REVOKE_SUCCESS
						.draft(access.getServerNames().get(server), order)
						.sendPrefixed(sender);
			}
		});
	}

}
