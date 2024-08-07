package me.szumielxd.portfel.proxy.commands;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.objects.PrizeToken;

public class DeletegiftcodeCommand<C> extends SimpleCommand<C> {
	
	@Getter private final List<CmdArg> staticArgs = List.of(CommonArgs.TOKEN);
	@Getter private final List<CmdArg> flyingArgs = List.of();

	public DeletegiftcodeCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "deletegiftcode", "deletegift", "deletecode");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		this.parseArguments(sender, args).ifPresent(parsed -> {
			PortfelProxyImpl<C> pl = (PortfelProxyImpl<C>) this.getPlugin();
			PrizeToken token = (PrizeToken)parsed.staticArgs()[0];
			pl.getTokenManager().deleteToken(token.getToken()).thenAccept(res -> {
				if (res.booleanValue()) {
					ProxyLangKey.COMMAND_DELETEGIFTCODE_SUCCESS.draft(token.getToken())
							.sendPrefixed(sender);
				} else {
					ProxyLangKey.COMMAND_DELETEGIFTCODE_FAIL.draft(token.getToken())
							.sendPrefixed(sender);
				}
			});
		});
	}

	@Override
	public @NotNull LangKey getDescription() {
		return ProxyLangKey.COMMAND_DELETEGIFTCODE_DESCRIPTION;
	}

}
