package me.szumielxd.portfel.bungee.commands.system;

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
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import net.kyori.adventure.text.Component;

public class UnregisterServerCommand extends SimpleCommand {
	
	public final List<CmdArg> args;

	public UnregisterServerCommand(@NotNull PortfelBungee plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "registerserver", "createserver");
		this.args = Arrays.asList(CommonArgs.SERVER);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		PortfelBungee pl = (PortfelBungee)this.getPlugin();
		AccessManager access = pl.getAccessManager();
		if (args.length == 0) return;
		// unregister
		CmdArg arg = this.args.get(0);
		UUID serverId = (UUID) arg.parseArg(args[0]);
		if (serverId == null) {
			sender.sendTranslated(arg.getArgError(Component.text(args[0], DARK_RED)));
			return;
		}
		access.unregister(serverId);
	}

	@Override
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) {
			String arg = args[0];
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
		return LangKey.COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION;
	}

}
