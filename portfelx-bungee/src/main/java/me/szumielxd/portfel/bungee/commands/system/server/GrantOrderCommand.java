package me.szumielxd.portfel.bungee.commands.system.server;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.commands.CommonArgs;
import me.szumielxd.portfel.bungee.managers.AccessManager;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class GrantOrderCommand extends SimpleCommand {
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.SERVER);

	public GrantOrderCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "grant");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		if (args.length > 0) {
			String order = (String) this.getArgs().get(0).parseArg(args[0]);
			if (order == null) {
				sender.sendTranslated(Portfel.PREFIX.append(this.getArgs().get(0).getArgError(Component.text(args[0], DARK_RED))));
				return;
			}
			UUID server = (UUID) parsedArgs[0];
			AccessManager access = ((PortfelBungee)this.getPlugin()).getAccessManager();
			if (access.canAccess(server, order)) {
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_SERVER_GRANT_ALREADY.component(RED)));
				return;
			}
			access.giveAccess(server, order);
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_SERVER_GRANT_SUCCESS.component(LIGHT_PURPLE,
					Component.text(access.getServerNames().get(server), AQUA),
					Component.text(order, AQUA))));
			return;
		}
		sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
	}

	@Override
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			return this.getArgs().get(0).getTabCompletions(sender).stream().filter(s -> s.toLowerCase().startsWith(arg)).sorted().collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION;
	}

}
