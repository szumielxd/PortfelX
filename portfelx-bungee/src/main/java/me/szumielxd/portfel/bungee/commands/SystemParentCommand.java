package me.szumielxd.portfel.bungee.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.commands.system.RegisterServerCommand;
import me.szumielxd.portfel.bungee.commands.system.ServerParentCommand;
import me.szumielxd.portfel.bungee.commands.system.UnregisterServerCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class SystemParentCommand extends ParentCommand {

	public SystemParentCommand(@NotNull PortfelBungeeImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "system", "sys");
		this.register(
				new RegisterServerCommand(plugin, this),
				new UnregisterServerCommand(plugin, this),
				new ServerParentCommand(plugin, this)
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
