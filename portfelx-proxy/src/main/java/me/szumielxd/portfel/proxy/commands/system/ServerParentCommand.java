package me.szumielxd.portfel.proxy.commands.system;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.commands.system.server.GrantOrderCommand;
import me.szumielxd.portfel.proxy.commands.system.server.RevokeOrderCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class ServerParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.SERVER);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_SYSTEM_SERVER_DESCRIPTION;
	
	
	public ServerParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "server", "srv");
		this.register(List.of(
				new GrantOrderCommand<>(plugin, this),
				new RevokeOrderCommand<>(plugin, this))
		);
	}

}
