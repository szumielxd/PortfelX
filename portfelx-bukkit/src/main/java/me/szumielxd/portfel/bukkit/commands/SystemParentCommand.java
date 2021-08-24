package me.szumielxd.portfel.bukkit.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.commands.system.ReloadCommand;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;

public class SystemParentCommand extends ParentCommand {

	public SystemParentCommand(@NotNull PortfelBukkitImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "system", "sys");
		this.register(
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
