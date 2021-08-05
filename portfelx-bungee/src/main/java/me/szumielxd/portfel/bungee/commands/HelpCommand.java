package me.szumielxd.portfel.bungee.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class HelpCommand extends SimpleCommand {
	

	public HelpCommand(@NotNull PortfelBungee plugin, @NotNull MainCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		
		String helpCmd = "/" + String.join(" ", label) + " help";
		PortfelBungee pl = (PortfelBungee) this.getPlugin();
		String pluginVersion = pl.getDescription().getName() + " " + pl.getDescription().getVersion();
		Component running = Portfel.PREFIX.append(LangKey.COMMAND_MAIN_RUNNING.component(DARK_PURPLE, Component.text(pluginVersion, LIGHT_PURPLE)));
		Component runningHover = Component.text(pluginVersion, LIGHT_PURPLE);
		//description
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_DESCRIPTION.component(AQUA))
				.append(Component.space())
				.append(Component.text(pl.getDescription().getDescription(), GRAY));
		//enabled
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_ENABLED.component(AQUA))
				.append(Component.space())
				.append(LangKey.MAIN_VALUE_YES.component(GREEN));
		//authors
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_AUTHORS.component(AQUA))
				.append(Component.space())
				.append(Component.text(pl.getDescription().getAuthor(), GRAY));
		sender.sendTranslated(running.hoverEvent(runningHover));
		
		if (args.length == 0) {
			Component use = Portfel.PREFIX.append(LangKey.COMMAND_MAIN_USE.component(DARK_AQUA, MiscUtils.buildCommandUsage(Component.text(helpCmd, AQUA), helpCmd, this)));
			sender.sendTranslated(use);
		} else {
			MainCommand parent = (MainCommand) this.getParent();
			parent.getChildrens().stream().sorted((a,b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName())).forEachOrdered(cmd -> {
				Lang lang = Lang.get(sender);
				String strCmd = "/" + String.join(" ", label) + cmd.getName();
				if (!cmd.getArgs().isEmpty()) strCmd += " " + String.join(" ", cmd.getArgs().stream().map(arg -> MiscUtils.argToCleanText(lang, arg)).toArray(String[]::new));
				Component line = Component.text("> ", LIGHT_PURPLE).append(Component.text(strCmd, AQUA));
				sender.sendTranslated(MiscUtils.buildCommandUsage(line, strCmd, cmd));
			});
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_HELP_DESCRIPTION;
	}

	
	
	
	
}
