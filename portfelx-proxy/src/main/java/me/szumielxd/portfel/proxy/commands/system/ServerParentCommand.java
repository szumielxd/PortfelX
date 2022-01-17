package me.szumielxd.portfel.proxy.commands.system;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.commands.system.server.GrantOrderCommand;
import me.szumielxd.portfel.proxy.commands.system.server.RevokeOrderCommand;

public class ServerParentCommand extends ParentCommand {
	
	public final List<CmdArg> args;

	public ServerParentCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "server", "srv");
		this.register(
				new GrantOrderCommand(plugin, this),
				new RevokeOrderCommand(plugin, this)
		);
		this.args = Arrays.asList(CommonArgs.SERVER);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_DESCRIPTION;
	}
	
	

}
