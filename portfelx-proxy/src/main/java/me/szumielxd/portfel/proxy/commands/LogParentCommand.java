package me.szumielxd.portfel.proxy.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.log.ReadLogCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class LogParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_LOG_DESCRIPTION;
	

	public LogParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "log", "logs");
		this.register(List.of(
				new ReadLogCommand<>(plugin, this)
		));
	}
	
	

}
