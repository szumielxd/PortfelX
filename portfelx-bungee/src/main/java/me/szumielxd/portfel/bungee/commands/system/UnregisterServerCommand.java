package me.szumielxd.portfel.bungee.commands.system;

import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.api.managers.AccessManager;
import me.szumielxd.portfel.bungee.commands.CommonArgs;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class UnregisterServerCommand extends SimpleCommand {
	
	public final List<CmdArg> args;

	public UnregisterServerCommand(@NotNull PortfelBungeeImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "unregisterserver", "deleteserver");
		this.args = Arrays.asList(CommonArgs.SERVER);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		PortfelBungeeImpl pl = (PortfelBungeeImpl)this.getPlugin();
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
