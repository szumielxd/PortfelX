package me.szumielxd.portfel.bungee.commands.user;

import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class UserInfoCommand extends SimpleCommand {

	public UserInfoCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		User user = (User) parsedArgs[0];
		Component header = Portfel.PREFIX.append(Component.text("> ", LIGHT_PURPLE, BOLD)).append(LangKey.COMMAND_USER_INFO_HEADER.component(DARK_PURPLE,
				this.prepareInteractive(Component.text(user.getName(), WHITE), label, user.getName())));
		String id = user.getUniqueId() != null? user.getUniqueId().toString() : "null";
		Component idType = MiscUtils.isOnlineModeUUID(user.getUniqueId()) ? LangKey.MAIN_VALUE_ONLINE.component(GREEN) : LangKey.MAIN_VALUE_OFFLINE.component(DARK_GRAY);
		Component uuid = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_USER_INFO_UUID.component(LIGHT_PURPLE,
				this.prepareInteractive(Component.text(id, WHITE), label, id)));
		Component uuidType = Portfel.PREFIX.append(Component.text("   ")).append(LangKey.COMMAND_USER_INFO_UUIDTYPE.component(GRAY, idType));
		Component status = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_USER_INFO_STATUS.component(DARK_PURPLE,
				user.isOnline()? LangKey.MAIN_VALUE_ONLINE.component(GREEN) : LangKey.MAIN_VALUE_OFFLINE.component(RED)));
		Component userdata = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_USER_INFO_USERDATA.component(LIGHT_PURPLE));
		Component balance = Portfel.PREFIX.append(Component.text("   ")).append(MiscUtils.bindCommand(LangKey.COMMAND_USER_INFO_BALANCE.component(DARK_PURPLE,
				Component.text(user.getBalance(), AQUA)), "/" + String.join(" ", Arrays.copyOf(label, label.length-1)) + " eco"));
		Component inTop = Portfel.PREFIX.append(Component.text("   ", WHITE)).append(LangKey.COMMAND_USER_INFO_INTOP.component(DARK_PURPLE,
				user.isDeniedInTop()? LangKey.MAIN_VALUE_FALSE.component(RED) : LangKey.MAIN_VALUE_TRUE.component(GREEN)));
		sender.sendTranslated(MiscUtils.join(Component.newline(), header, uuid, uuidType, status, userdata, balance, inTop));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_INFO_DESCRIPTION;
	}
	
	private Component prepareInteractive(Component comp, String[] label, String text) {
		return comp.hoverEvent(Component.text(text, AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand("/" + String.join(" ", Arrays.copyOf(label, 2)) + "" + text)).insertion(text);
	}

}