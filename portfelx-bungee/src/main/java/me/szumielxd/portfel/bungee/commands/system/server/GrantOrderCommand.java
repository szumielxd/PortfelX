package me.szumielxd.portfel.bungee.commands.system.server;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.commands.CommonArgs;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;

public class GrantOrderCommand extends SimpleCommand {
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.SERVER);

	public GrantOrderCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "grant");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		return null;
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
