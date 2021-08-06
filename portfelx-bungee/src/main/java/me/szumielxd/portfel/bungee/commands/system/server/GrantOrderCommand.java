package me.szumielxd.portfel.bungee.commands.system.server;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			String order = (String) parsed[0];
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
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_SERVER_GRANT_DESCRIPTION;
	}

}
