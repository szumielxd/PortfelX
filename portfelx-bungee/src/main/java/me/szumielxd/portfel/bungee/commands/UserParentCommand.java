package me.szumielxd.portfel.bungee.commands;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class UserParentCommand extends ParentCommand {
	
	
	public List<CmdArg> args = Arrays.asList(CommonArgs.USER);
	

	public UserParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "user");
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.getArgs();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_DESCRIPTION;
	}

}
