package me.szumielxd.portfel.proxy.commands.system;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class UnregisterServerCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.SERVER);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION;
	

	public UnregisterServerCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "unregisterserver", "deleteserver");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		parseArguments(sender, parsedContext).ifPresent(parsed -> {
			PortfelProxyImpl<C> pl = (PortfelProxyImpl<C>)this.getPlugin();
			AccessManager access = pl.getAccessManager();
			UUID serverId = (UUID) parsed.parsedArgs().staticArgs()[0];
			String srvId = serverId.toString();
			String srvName = access.getServerNames().get(serverId);
			
			access.unregister(serverId);
			
			ProxyLangKey.COMMAND_SYSTEM_UNREGISTERSERVER_SUCCESS
					.draft(
							ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(srvName, ProxyLangKey.SERVER_FIELD_FRIENDLYNAME),
							ProxyLangKey.MAIN_MESSAGE_INSERTION.draft(srvId, ProxyLangKey.SERVER_FIELD_ID))
					.sendPrefixed(sender);
		});
	}

}
