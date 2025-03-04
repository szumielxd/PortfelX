package me.szumielxd.portfel.proxy.commands.user.eco;

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
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class EcoGiveCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(CommonArgs.ECO_AMOUNT, CommonArgs.REASON);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_ECO_GIVE_DESCRIPTION;
	

	public EcoGiveCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "give", "add");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		parseArguments(sender, parsedContext).ifPresent(parsed -> {
			Long amount = (Long) parsed.parsedStaticArg(0);
			String reason = (String) parsed.parsedStaticArg(1);
			User user = (User) parsedContext.parsedStaticArg(0);
			user.addBalance(amount, ProxyActionExecutor.sender(sender), "Proxy", reason).whenComplete((res, ex) -> {
				if (ex == null) {
					ProxyLangKey.COMMAND_USER_ECO_GIVE_SUCCESS
							.draft(
									user.getName(),
									MainLangKey.MAIN_CURRENCY_FORMAT.draft(amount))
							.sendPrefixed(sender);
				} else {
					MainLangKey.ERROR_COMMAND_EXECUTION.draft()
							.sendPrefixed(sender);
					getPlugin().logger().severe(ex, "An error occurred while executing eco give command");
				}
			});
		});
	}

}
