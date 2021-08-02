package me.szumielxd.portfel.bungee.commands.system;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.managers.AccessManager;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonPlayer;
import me.szumielxd.portfel.common.objects.CommonSender;

public class RegisterServerCommand extends SimpleCommand {
	
	public final List<CmdArg> args;

	public RegisterServerCommand(@NotNull PortfelBungee plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "registerserver", "createserver");
		this.args = Arrays.asList(
				// serverName
				new CmdArg(LangKey.COMMAND_ARGTYPES_SERVERNAME_DISPLAY, LangKey.COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION, LangKey.EMPTY, s -> s, s -> new ArrayList<>())
		);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		PortfelBungee pl = (PortfelBungee)this.getPlugin();
		AccessManager access = pl.getAccessManager();
		if (args.length == 0) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_NEEDED.component(RED)));
			return;
		}
		if (access.getServerByName(args[0]) != null) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY.component(RED)));
			return;
		}
		// register
		pl.getAccessManager().pendingRegistration((CommonPlayer) sender, args[0]);
	}

	@Override
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) return this.getArgs().get(0).getTabCompletions(sender);
		return new ArrayList<>();
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_REGISTERSERVER_DESCRIPTION;
	}
	
	@Override
	public @NotNull CommandAccess getAccess() {
		return CommandAccess.PLAYERS;
	}

}
