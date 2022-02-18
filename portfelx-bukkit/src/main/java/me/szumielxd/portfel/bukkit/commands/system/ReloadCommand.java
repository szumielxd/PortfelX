package me.szumielxd.portfel.bukkit.commands.system;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class ReloadCommand extends SimpleCommand {

	public ReloadCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "reload", "rl");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		PortfelBukkitImpl plugin = (PortfelBukkitImpl) this.getPlugin();
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_RELOAD_EXECUTE.component(GRAY)));
		try {
			plugin.unload();
			plugin.load();
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_RELOAD_ERROR.component(DARK_RED)).hoverEvent(Component.text(sw.toString(), RED)));
			return;
		}
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_RELOAD_SUCCESS.component(GREEN, Component.text(plugin.getName()+" "+plugin.asPlugin().getDescription().getVersion(), AQUA))));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_RELOAD_DESCRIPTION;
	}
	
	private @NotNull Component prepareInteractive(@NotNull Component comp, @NotNull String[] label, @NotNull String text) {
		return comp.hoverEvent(Component.text(text, AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand("/" + String.join(" ", Arrays.copyOf(label, 2)) + " " + text)).insertion(text);
	}

}