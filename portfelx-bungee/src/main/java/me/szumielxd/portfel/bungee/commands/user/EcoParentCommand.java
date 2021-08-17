package me.szumielxd.portfel.bungee.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.bungee.commands.user.eco.EcoGiveCommand;
import me.szumielxd.portfel.bungee.commands.user.eco.EcoSetCommand;
import me.szumielxd.portfel.bungee.commands.user.eco.EcoTakeCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class EcoParentCommand extends ParentCommand {

	public EcoParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "eco", "economy", "bal", "balance");
		this.register(
				new EcoGiveCommand(plugin, this),
				new EcoSetCommand(plugin, this),
				new EcoTakeCommand(plugin, this)
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
