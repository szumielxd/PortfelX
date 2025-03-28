package me.szumielxd.portfel.bukkit.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.commands.system.ReloadCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import net.kyori.adventure.text.Component;

public class SystemParentCommand extends ParentCommand<Component> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = MainLangKey.COMMAND_SYSTEM_DESCRIPTION;
	

	public SystemParentCommand(@NotNull PortfelBukkitImpl plugin, @NotNull ParentLikeCommand<Component> parent) {
		super(plugin, parent, "system", "sys");
		this.register(List.of(
				new ReloadCommand(plugin, this)));
	}

}
