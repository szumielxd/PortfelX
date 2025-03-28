package me.szumielxd.portfel.proxy.commands.user.minoreco;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class MinorEcoGiveCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.ECO_AMOUNT);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_MINORECO_GIVE_DESCRIPTION;
	
	
	public MinorEcoGiveCommand(@NotNull Portfel<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "give", "add");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		this.parseArguments(sender, parsedContext).ifPresent(parsed -> {
			Long amount = (Long) parsed.parsedArgs().staticArgs()[0];
			User user = (User) parsedContext.parsedArgs().staticArgs()[0];
			user.giveMinorBalance(amount);
			ProxyLangKey.COMMAND_USER_MINORECO_GIVE_SUCCESS
					.draft(
							user.getName(),
							MainLangKey.MAIN_MINORCURRENCY_FORMAT.draft(amount))
					.sendPrefixed(sender);
		});
	}

}
