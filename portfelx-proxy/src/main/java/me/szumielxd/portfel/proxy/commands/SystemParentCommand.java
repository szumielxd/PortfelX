package me.szumielxd.portfel.proxy.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.system.RegisterServerCommand;
import me.szumielxd.portfel.proxy.commands.system.ReloadCommand;
import me.szumielxd.portfel.proxy.commands.system.ServerParentCommand;
import me.szumielxd.portfel.proxy.commands.system.UnregisterServerCommand;

public class SystemParentCommand extends ParentCommand {

	public SystemParentCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "system", "sys");
		this.register(
				new RegisterServerCommand(plugin, this),
				new UnregisterServerCommand(plugin, this),
				new ServerParentCommand(plugin, this),
				new ReloadCommand(plugin, this)
		);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_DESCRIPTION;
	}
	
	

}
