package me.szumielxd.portfel.proxy.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoGiveCommand;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoSetCommand;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoTakeCommand;

public class MinorEcoParentCommand extends ParentCommand {

	public MinorEcoParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "meco", "minoreconomy", "mbal", "minorbalance");
		this.register(
				new MinorEcoGiveCommand(plugin, this),
				new MinorEcoSetCommand(plugin, this),
				new MinorEcoTakeCommand(plugin, this)
		);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_MINORECO_DESCRIPTION;
	}

}
