package me.szumielxd.portfel.proxy.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.commands.user.eco.EcoGiveCommand;
import me.szumielxd.portfel.proxy.commands.user.eco.EcoSetCommand;
import me.szumielxd.portfel.proxy.commands.user.eco.EcoTakeCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class EcoParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_ECO_DESCRIPTION;
	

	public EcoParentCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "eco", "economy", "bal", "balance");
		this.register(List.of(
				new EcoGiveCommand<>(plugin, this),
				new EcoSetCommand<>(plugin, this),
				new EcoTakeCommand<>(plugin, this)
		));
	}

}
