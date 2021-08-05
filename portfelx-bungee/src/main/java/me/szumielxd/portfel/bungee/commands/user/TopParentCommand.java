package me.szumielxd.portfel.bungee.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.commands.user.top.TopInfoCommand;
import me.szumielxd.portfel.bungee.commands.user.top.TopSetCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class TopParentCommand extends ParentCommand {

	
	
	public TopParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "top");
		this.register(
				new TopInfoCommand(plugin, this),
				new TopSetCommand(plugin, this)
		);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_ARGTYPES_INTOP_DESCRIPTION;
	}

}
