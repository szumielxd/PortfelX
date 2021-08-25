package me.szumielxd.portfel.bungee.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.PrizeToken;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import net.kyori.adventure.text.Component;

public class DeletegiftcodeCommand extends SimpleCommand {
	
	public final List<CmdArg> args = Arrays.asList(CommonArgs.TOKEN);

	public DeletegiftcodeCommand(@NotNull PortfelBungeeImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "deletegiftcode", "deletegift", "deletecode");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		PortfelBungeeImpl pl = (PortfelBungeeImpl)this.getPlugin();
		PrizeToken token = (PrizeToken)parsed[0];
		try {
			if (pl.getTokenManager().deleteToken(token.getToken()).get()) {
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_DELETEGIFTCODE_SUCCESS.component(GREEN, Component.text(token.getToken(), AQUA))));
				return;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_DELETEGIFTCODE_FAIL.component(RED, Component.text(token.getToken(), AQUA))));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_DELETEGIFTCODE_DESCRIPTION;
	}

}
