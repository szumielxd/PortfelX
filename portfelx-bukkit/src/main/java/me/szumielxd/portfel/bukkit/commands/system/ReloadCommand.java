package me.szumielxd.portfel.bukkit.commands.system;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import net.kyori.adventure.text.Component;

public class ReloadCommand extends SimpleCommand<Component> {

	public ReloadCommand(@NotNull PortfelBukkitImpl plugin, @NotNull AbstractCommand<Component> parent) {
		super(plugin, parent, "reload", "rl");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(@NotNull CommonSender<Component> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		PortfelBukkitImpl plugin = (PortfelBukkitImpl) this.getPlugin();
		MainLangKey.COMMAND_SYSTEM_RELOAD_EXECUTE
				.draft()
				.send(sender, true);
		try {
			plugin.unload();
			plugin.load();
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			MainLangKey.COMMAND_SYSTEM_RELOAD_ERROR
					.draft(sw.toString())
					.send(sender, true);
			return;
		}
		MainLangKey.COMMAND_SYSTEM_RELOAD_SUCCESS
				.draft(plugin.getName(), plugin.getDescription().getVersion())
				.send(sender, true);
	}

	@Override
	public @NotNull List<CmdArg> getStaticArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull List<CmdArg> getFlyingArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return MainLangKey.COMMAND_SYSTEM_RELOAD_DESCRIPTION;
	}

}