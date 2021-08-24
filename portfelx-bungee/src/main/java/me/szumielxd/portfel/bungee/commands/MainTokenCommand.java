package me.szumielxd.portfel.bungee.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Collections;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeeSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MainTokenCommand extends Command implements TabExecutor {

	
	private final PortfelBungeeImpl plugin;
	
	
	public MainTokenCommand(PortfelBungeeImpl plugin, String name, String[] aliases) {
		super(name, "portfel.token-command", aliases);
		this.plugin = plugin;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		CommonSender s = BungeeSender.get(this.plugin, sender);
		if (!(sender instanceof ProxiedPlayer)) {
			s.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_PLAYERS_ONLY.component(RED)));
			return;
		}
		if (args.length != 1) {
			s.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_USAGE.component(RED, Component.text(this.getName()))));
			return;
		}
		this.plugin.getTokenManager().tryValidateToken((ProxiedPlayer) sender, args[0]);
	}

}
