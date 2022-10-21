package me.szumielxd.portfel.proxy.commands.user.minoreco;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import net.kyori.adventure.text.Component;

public class MinorEcoSetCommand extends SimpleCommand {
	
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.ECO_AMOUNT);
	

	public MinorEcoSetCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "set");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			Long amount = (Long) parsed[0];
			User user = (User) parsedArgs[0];
			user.setMinorBalance(amount);
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_USER_MINORECO_SET_SUCCESS.component(LIGHT_PURPLE,
					Component.text(user.getName(), AQUA), LangKey.MAIN_CURRENCY_FORMAT.component(AQUA, Component.text(amount)))));
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_MINORECO_SET_DESCRIPTION;
	}

}
