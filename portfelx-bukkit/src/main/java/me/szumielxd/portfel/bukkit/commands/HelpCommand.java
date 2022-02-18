package me.szumielxd.portfel.bukkit.commands;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class HelpCommand extends SimpleCommand {
	

	public HelpCommand(@NotNull PortfelBukkitImpl plugin, @NotNull MainCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		
		String[] shortLabel = Arrays.copyOf(label, label.length-1);
		String helpCmd = "/" + String.join(" ", shortLabel) + " help";
		PortfelBukkitImpl pl = (PortfelBukkitImpl) this.getPlugin();
		String pluginVersion = pl.asPlugin().getDescription().getName() + " " + pl.asPlugin().getDescription().getVersion();
		Component running = Portfel.PREFIX.append(LangKey.COMMAND_MAIN_RUNNING.component(DARK_PURPLE, Component.text(pluginVersion, LIGHT_PURPLE)));
		Component runningHover = Component.text(pluginVersion, LIGHT_PURPLE);
		//description
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_DESCRIPTION.component(AQUA))
				.append(Component.space())
				.append(Component.text(pl.asPlugin().getDescription().getDescription(), GRAY));
		//enabled
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_ENABLED.component(AQUA))
				.append(Component.space())
				.append(LangKey.MAIN_VALUE_YES.component(GREEN));
		//authors
		runningHover = runningHover.append(Component.newline()).append(LangKey.MAIN_VALUENAME_AUTHORS.component(AQUA))
				.append(Component.space())
				.append(Component.text(String.join(", ", pl.asPlugin().getDescription().getAuthors()), GRAY));
		sender.sendTranslated(running.hoverEvent(runningHover));
		if (!label[shortLabel.length].equals("")) {
			MainCommand parent = (MainCommand) this.getParent();
			parent.getChildrens().stream().sorted((a,b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName())).forEachOrdered(cmd -> {
				Lang lang = Lang.get(sender);
				String strCmd = "/" + String.join(" ", shortLabel) + " " + cmd.getName();
				if (!cmd.getArgs().isEmpty()) strCmd += " " + String.join(" ", cmd.getArgs().stream().map(arg -> MiscUtils.argToCleanText(lang, arg)).toArray(String[]::new));
				Component line = Component.text("> ", LIGHT_PURPLE).append(Component.text(strCmd, AQUA));
				sender.sendTranslated(MiscUtils.buildCommandUsage(line, strCmd, cmd));
			});
		} else {
			Component use = Portfel.PREFIX.append(LangKey.COMMAND_MAIN_USE.component(DARK_AQUA, MiscUtils.buildCommandUsage(Component.text(helpCmd, AQUA), helpCmd, this)));
			sender.sendTranslated(use);
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
