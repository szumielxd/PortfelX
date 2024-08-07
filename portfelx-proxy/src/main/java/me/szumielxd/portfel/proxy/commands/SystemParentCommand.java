package me.szumielxd.portfel.proxy.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.system.RegisterServerCommand;
import me.szumielxd.portfel.proxy.commands.system.ReloadCommand;
import me.szumielxd.portfel.proxy.commands.system.ServerParentCommand;
import me.szumielxd.portfel.proxy.commands.system.UnregisterServerCommand;

public class SystemParentCommand<C> extends ParentCommand<C> {

	public SystemParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "system", "sys");
		this.register(List.of(
				new RegisterServerCommand<>(plugin, this),
				new UnregisterServerCommand<>(plugin, this),
				new ServerParentCommand<>(plugin, this),
				new ReloadCommand<>(plugin, this)
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
		return MainLangKey.COMMAND_SYSTEM_DESCRIPTION;
	}

}
