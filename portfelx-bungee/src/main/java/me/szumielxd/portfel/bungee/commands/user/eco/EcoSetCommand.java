package me.szumielxd.portfel.bungee.commands.user.eco;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.commands.CommonArgs;
import me.szumielxd.portfel.bungee.objects.BungeeActionExecutor;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class EcoSetCommand extends SimpleCommand {
	
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.ECO_AMOUNT, CommonArgs.ORDER);
	

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
			CompletableFuture<Exception> future = user.setBalance(amount, BungeeActionExecutor.sender(sender), "Proxy", reason);
			try {
				Exception ex = future.get();
				if (ex != null) {
					sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(RED)));
					ex.printStackTrace();
					return;
				}
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_USER_ECO_SET_SUCCESS.component(LIGHT_PURPLE,
						Component.text(user.getName(), AQUA), Component.text(amount, AQUA))));
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
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
