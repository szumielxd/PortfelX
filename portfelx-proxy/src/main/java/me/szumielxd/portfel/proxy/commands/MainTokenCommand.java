package me.szumielxd.portfel.proxy.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import net.kyori.adventure.text.Component;

public class MainTokenCommand extends CommonCommand {

	
	private final PortfelProxyImpl plugin;
	
	
	public MainTokenCommand(PortfelProxyImpl plugin, String name, String[] aliases) {
		super(name, "portfel.token-command", aliases);
		this.plugin = plugin;
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull ProxySender sender, @NotNull String[] args) {
		return Collections.emptyList();
	}

	@Override
	public void execute(@NotNull ProxySender sender, @NotNull String[] args) {
		if (!(sender instanceof ProxyPlayer)) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_PLAYERS_ONLY.component(RED)));
			return;
		}
		if (args.length != 1) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.TOKEN_CHECK_USAGE.component(RED, Component.text(this.getName()))));
			return;
		}
		this.plugin.getTokenManager().tryValidateToken((ProxyPlayer) sender, args[0]);
	}

}
