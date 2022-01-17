package me.szumielxd.portfel.proxy.commands.system;

import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.managers.AccessManager;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class UnregisterServerCommand extends SimpleCommand {
	
	public final List<CmdArg> args;

	public UnregisterServerCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "unregisterserver", "deleteserver");
		this.args = Arrays.asList(CommonArgs.SERVER);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		PortfelProxyImpl pl = (PortfelProxyImpl)this.getPlugin();
		AccessManager access = pl.getAccessManager();
		UUID serverId = (UUID) parsed[0];
		access.unregister(serverId);
		
		String srvId = serverId.toString();
		String srvName = access.getServerNames().get(serverId);
		Component srvIdComp = Component.text(srvId, AQUA, UNDERLINED)
				.clickEvent(ClickEvent.suggestCommand(srvId)).insertion(srvId)
				.hoverEvent(Component.text("» ", DARK_AQUA).append(LangKey.MAIN_MESSAGE_INSERTION
						.component(AQUA, Component.text("server ID"))
				));
		Component srvNameComp = Component.text(srvName, AQUA, UNDERLINED)
				.clickEvent(ClickEvent.suggestCommand(srvName)).insertion(srvName)
				.hoverEvent(Component.text("» ", DARK_AQUA).append(LangKey.MAIN_MESSAGE_INSERTION
						.component(AQUA, Component.text("server friendly name"))
				));
		
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_SYSTEM_UNREGISTERSERVER_SUCCESS
				.component(LIGHT_PURPLE, srvIdComp, srvNameComp)));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_SYSTEM_UNREGISTERSERVER_DESCRIPTION;
	}

}
