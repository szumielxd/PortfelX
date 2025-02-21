package me.szumielxd.portfel.proxy.commands.system;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;

public class ReloadCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = MainLangKey.COMMAND_SYSTEM_RELOAD_DESCRIPTION;
	

	public ReloadCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "reload", "rl");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		PortfelProxyImpl<C> plugin = (PortfelProxyImpl<C>) this.getPlugin();
		MainLangKey.COMMAND_SYSTEM_RELOAD_EXECUTE
				.draft()
				.sendPrefixed(sender);
		try {
			plugin.unload();
			plugin.load();
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			getPlugin().getLogger().log(Level.SEVERE, "Ann error occurred while executing top info command", e);
			MainLangKey.COMMAND_SYSTEM_RELOAD_ERROR
					.draft(sw.toString())
					.sendPrefixed(sender);
			return;
		}
		MainLangKey.COMMAND_SYSTEM_RELOAD_SUCCESS
				.draft(plugin.getName(), plugin.getVersion())
				.sendPrefixed(sender);
	}

}