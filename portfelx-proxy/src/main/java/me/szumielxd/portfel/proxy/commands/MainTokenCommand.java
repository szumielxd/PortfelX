package me.szumielxd.portfel.proxy.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class MainTokenCommand<C> extends CommonCommand<C> {

	
	private final PortfelProxyImpl<C> plugin;
	
	
	public MainTokenCommand(PortfelProxyImpl<C> plugin, String name, String[] aliases) {
		super(name, "portfel.token-command", aliases);
		this.plugin = plugin;
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		return Collections.emptyList();
	}

	@Override
	public void execute(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		if (sender instanceof ProxyPlayer<C> player) {
			if (args.length == 1) {
				this.plugin.getTokenManager().tryValidateToken(player, args[0]);
			} else {
				ProxyLangKey.TOKEN_CHECK_USAGE.draft(getName())
						.sendPrefixed(sender);
			}
		} else {
			MainLangKey.ERROR_COMMAND_PLAYERS_ONLY.draft()
					.sendPrefixed(sender);
		}
		
	}

}
