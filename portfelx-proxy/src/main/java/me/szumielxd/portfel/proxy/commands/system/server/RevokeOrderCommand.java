package me.szumielxd.portfel.proxy.commands.system.server;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import net.kyori.adventure.text.Component;

public class RevokeOrderCommand extends SimpleCommand {
	
	private final List<CmdArg> args = Arrays.asList(
			new CmdArg(LangKey.COMMAND_ARGTYPES_ORDER_DISPLAY, LangKey.COMMAND_ARGTYPES_ORDER_DESCRIPTION, LangKey.EMPTY, s -> s.toLowerCase(), (s, label) -> {
				UUID server = (UUID) CommonArgs.SERVER.parseArg(label[3]);
				if (server == null) return new ArrayList<>();
				return ((PortfelProxyImpl)this.getPlugin()).getAccessManager().getAllowedOrders(server);
			})
	);

	public RevokeOrderCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "revoke");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			String order = (String) parsed[0];
			UUID server = (UUID) parsedArgs[0];
			AccessManager access = ((PortfelProxyImpl)this.getPlugin()).getAccessManager();
			if (!access.canAccess(server, order)) {
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_SERVER_REVOKE_ALREADY.component(RED)));
				return;
			}
			access.takeAccess(server, order);
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_SERVER_REVOKE_SUCCESS.component(LIGHT_PURPLE,
					Component.text(access.getServerNames().get(server), AQUA),
					Component.text(order, AQUA))));
			return;
		}
		sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_SERVER_REVOKE_DESCRIPTION;
	}

}
