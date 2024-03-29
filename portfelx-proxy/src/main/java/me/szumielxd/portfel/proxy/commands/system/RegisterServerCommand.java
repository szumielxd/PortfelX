package me.szumielxd.portfel.proxy.commands.system;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;

public class RegisterServerCommand extends SimpleCommand {
	
	public final List<CmdArg> args;

	public RegisterServerCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "registerserver", "createserver");
		this.args = Arrays.asList(
				// serverName
				new CmdArg(LangKey.COMMAND_ARGTYPES_SERVERNAME_DISPLAY, LangKey.COMMAND_ARGTYPES_SERVERNAME_DESCRIPTION, LangKey.EMPTY, s -> s, s -> new ArrayList<>()),
				// hashKey
				new CmdArg(LangKey.COMMAND_ARGTYPES_HASHKEY_DISPLAY, LangKey.COMMAND_ARGTYPES_HASHKEY_DESCRIPTION, LangKey.EMPTY, s -> s, s -> new ArrayList<>())
		);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		PortfelProxyImpl pl = (PortfelProxyImpl)this.getPlugin();
		AccessManager access = pl.getAccessManager();
		if (access.getServerByName(args[0]) != null) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_REGISTERSERVER_SERVERNAME_ALREADY.component(RED)));
			return;
		}
		pl.getAccessManager().pendingRegistration((ProxyPlayer) sender, args[0], args[1]);
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
