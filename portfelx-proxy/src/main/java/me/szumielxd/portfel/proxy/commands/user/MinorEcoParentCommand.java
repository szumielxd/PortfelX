package me.szumielxd.portfel.proxy.commands.user;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoGiveCommand;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoSetCommand;
import me.szumielxd.portfel.proxy.commands.user.minoreco.MinorEcoTakeCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class MinorEcoParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_MINORECO_DESCRIPTION;
	
	
	public MinorEcoParentCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "meco", "minoreconomy", "mbal", "minorbalance");
		this.register(List.of(
				new MinorEcoGiveCommand<>(plugin, this),
				new MinorEcoSetCommand<>(plugin, this),
				new MinorEcoTakeCommand<>(plugin, this)
		));
	}

}
