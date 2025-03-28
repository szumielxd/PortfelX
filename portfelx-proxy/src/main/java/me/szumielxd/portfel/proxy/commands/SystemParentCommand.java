package me.szumielxd.portfel.proxy.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.system.RegisterServerCommand;
import me.szumielxd.portfel.proxy.commands.system.ReloadCommand;
import me.szumielxd.portfel.proxy.commands.system.ServerParentCommand;
import me.szumielxd.portfel.proxy.commands.system.UnregisterServerCommand;

public class SystemParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = MainLangKey.COMMAND_SYSTEM_DESCRIPTION;
	

	public SystemParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "system", "sys");
		this.register(List.of(
				new RegisterServerCommand<>(plugin, this),
				new UnregisterServerCommand<>(plugin, this),
				new ServerParentCommand<>(plugin, this),
				new ReloadCommand<>(plugin, this)
		));
	}

}
