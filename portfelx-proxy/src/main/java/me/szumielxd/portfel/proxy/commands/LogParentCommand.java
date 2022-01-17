package me.szumielxd.portfel.proxy.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.log.ReadLogCommand;

public class LogParentCommand extends ParentCommand {

	public LogParentCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "log", "logs");
		this.register(
				new ReadLogCommand(plugin, this)
		);
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_LOG_DESCRIPTION;
	}
	
	

}
