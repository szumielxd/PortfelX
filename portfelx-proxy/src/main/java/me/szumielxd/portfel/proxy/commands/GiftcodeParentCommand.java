package me.szumielxd.portfel.proxy.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.commands.giftcode.GiftcodeInfoCommand;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class GiftcodeParentCommand<C> extends ParentCommand<C> {
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.TOKEN);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_GIFTCODE_DESCRIPTION;
	

	public GiftcodeParentCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "giftcode", "gift", "code", "token");
		this.register(List.of(
				new GiftcodeInfoCommand<>(plugin, this)
		));
	}

}
