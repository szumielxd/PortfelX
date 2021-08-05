package me.szumielxd.portfel.bungee.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.commands.user.eco.EcoSetCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class EcoParentCommand extends ParentCommand {

	public EcoParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent, @NotNull String name, @NotNull String[] aliases) {
		super(plugin, parent, name, aliases);
		this.register(
				new EcoSetCommand(plugin, this)
		);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_ECO_DESCRIPTION;
	}

}
