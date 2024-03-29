package me.szumielxd.portfel.proxy.commands.user.eco;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import net.kyori.adventure.text.Component;

public class EcoSetCommand extends SimpleCommand {
	
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.ECO_AMOUNT, CommonArgs.REASON);
	

	public EcoSetCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "set");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			Long amount = (Long) parsed[0];
			String reason = (String) parsed[1];
			User user = (User) parsedArgs[0];
			CompletableFuture<Exception> future = user.setBalance(amount, ProxyActionExecutor.sender(sender), "Proxy", reason);
			try {
				Exception ex = future.get();
				if (ex != null) throw ex;
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_USER_ECO_SET_SUCCESS.component(LIGHT_PURPLE,
						Component.text(user.getName(), AQUA), LangKey.MAIN_CURRENCY_FORMAT.component(AQUA, Component.text(amount)))));
				
			} catch (Exception e) {
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(DARK_RED)));
				e.printStackTrace();
			}
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_ECO_SET_DESCRIPTION;
	}

}
