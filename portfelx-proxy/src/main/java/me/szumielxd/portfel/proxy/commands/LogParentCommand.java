package me.szumielxd.portfel.proxy.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.log.ReadLogCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class LogParentCommand<C> extends ParentCommand<C> {

	public LogParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "log", "logs");
		this.register(List.of(
				new ReadLogCommand<>(plugin, this)
		));
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
		return ProxyLangKey.COMMAND_LOG_DESCRIPTION;
	}
	
	

}
