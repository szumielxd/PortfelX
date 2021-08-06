package me.szumielxd.portfel.bungee.commands.user.top;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class TopInfoCommand extends SimpleCommand {

	public TopInfoCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		User user = (User) parsedArgs[0];
		try {
			int pos = ((PortfelBungee)this.getPlugin()).getDB().getTopPos(user)[0];
			Component inTop = Portfel.PREFIX.append(LangKey.COMMAND_USER_TOP_INFO_INTOP.component(DARK_PURPLE, Component.text(user.getName())));
			Component inTopValue = Portfel.PREFIX.append(Component.text("-> ", LIGHT_PURPLE))
					.append(user.isDeniedInTop() ? LangKey.MAIN_VALUE_FALSE.component(RED) : LangKey.MAIN_VALUE_TRUE.component(GREEN));
			Component position = Portfel.PREFIX.append(LangKey.COMMAND_USER_TOP_INFO_POSITION.component(DARK_PURPLE, Component.text(user.getName())));
			Component positionValue = Portfel.PREFIX.append(Component.text("-> ", LIGHT_PURPLE)).append(Component.text(pos, AQUA));
			sender.sendTranslated(MiscUtils.join(Component.newline(), inTop, inTopValue, position, positionValue));
		} catch (Exception e) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(DARK_RED)));
			e.printStackTrace();
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_TOP_INFO_DESCRIPTION;
	}

}