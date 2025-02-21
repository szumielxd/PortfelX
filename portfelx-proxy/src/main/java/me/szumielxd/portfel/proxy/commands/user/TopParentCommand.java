package me.szumielxd.portfel.proxy.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.commands.user.top.TopInfoCommand;
import me.szumielxd.portfel.proxy.commands.user.top.TopSetCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class TopParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_ARGTYPES_INTOP_DESCRIPTION;
		
	
	public TopParentCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "top");
		this.register(List.of(
				new TopInfoCommand<>(plugin, this),
				new TopSetCommand<>(plugin, this)));
	}

}
