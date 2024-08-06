package me.szumielxd.portfel.bukkit.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.commands.system.ReloadCommand;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import net.kyori.adventure.text.Component;

public class SystemParentCommand extends ParentCommand<Component> {

	public SystemParentCommand(@NotNull PortfelBukkitImpl plugin, @NotNull AbstractCommand<Component> parent) {
		super(plugin, parent, "system", "sys");
		this.register(List.of(
				new ReloadCommand(plugin, this)));
	}

	@Override
	public @NotNull List<CmdArg> getStaticArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull List<CmdArg> getFlyingArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return BukkitLangKey.COMMAND_SYSTEM_DESCRIPTION;
	}
	
	

}
