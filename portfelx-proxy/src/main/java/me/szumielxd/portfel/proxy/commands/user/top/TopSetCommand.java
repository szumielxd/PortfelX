package me.szumielxd.portfel.proxy.commands.user.top;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class TopSetCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.INTOP);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_TOP_SET_DESCRIPTION;
	
	
	public TopSetCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "set");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		User user = (User) parsedContext.parsedArgs().staticArgs()[0];
		this.parseArguments(sender, parsedContext.argsLeft()).ifPresent(newParsedContext -> {
			boolean allowed = (boolean) newParsedContext.parsedArgs().staticArgs()[0];
			if (user.isDeniedInTop() != allowed) {
				ProxyLangKey.COMMAND_USER_TOP_SET_ALREADY
						.draft(user.getName(), allowed)
						.sendPrefixed(sender);
				return;
			} else {
				user.setDeniedInTop(!allowed).whenComplete((res, ex) -> {
					if (ex == null) {
						ProxyLangKey.COMMAND_USER_TOP_SET_SUCCESS
								.draft(user.getName(), allowed)
								.sendPrefixed(sender);
					} else {
						MainLangKey.ERROR_COMMAND_EXECUTION.draft()
								.sendPrefixed(sender);
						getPlugin().logger().severe(ex, "An error occurred while executing top info command");
					}
				});
			}
		});
	}

}
