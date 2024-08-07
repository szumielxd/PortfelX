package me.szumielxd.portfel.proxy.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.commands.user.EcoParentCommand;
import me.szumielxd.portfel.proxy.commands.user.MinorEcoParentCommand;
import me.szumielxd.portfel.proxy.commands.user.TopParentCommand;
import me.szumielxd.portfel.proxy.commands.user.UserInfoCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class UserParentCommand<C> extends ParentCommand<C> {
	
	
	@Getter private final List<CmdArg> staticArgs = List.of(CommonArgs.USER);
	@Getter private final List<CmdArg> flyingArgs = List.of();
	

	public UserParentCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "user");
		this.register(List.of(
				new UserInfoCommand<>(plugin, this),
				new EcoParentCommand<>(plugin, this),
				new MinorEcoParentCommand<>(plugin, this),
				new TopParentCommand<>(plugin, this)
		));
	}

	@Override
	public @NotNull LangKey getDescription() {
		return ProxyLangKey.COMMAND_USER_DESCRIPTION;
	}

}
