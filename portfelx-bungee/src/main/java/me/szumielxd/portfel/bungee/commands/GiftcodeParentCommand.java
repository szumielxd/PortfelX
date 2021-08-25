package me.szumielxd.portfel.bungee.commands;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.commands.giftcode.GiftcodeInfoCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class GiftcodeParentCommand extends ParentCommand {
	
	public List<CmdArg> args = Arrays.asList(CommonArgs.TOKEN);

	public GiftcodeParentCommand(@NotNull PortfelBungeeImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "giftcode", "gift", "code", "token");
		this.register(
				new GiftcodeInfoCommand(plugin, this)
		);
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
