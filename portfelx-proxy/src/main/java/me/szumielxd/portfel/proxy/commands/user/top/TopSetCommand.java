package me.szumielxd.portfel.proxy.commands.user.top;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import net.kyori.adventure.text.Component;

public class TopSetCommand extends SimpleCommand {
	
	
	private final List<CmdArg> args = Arrays.asList(CommonArgs.INTOP);
	

	public TopSetCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "set");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		User user = (User) parsedArgs[0];
		boolean denied = !(boolean)parsed[0];
		Component name = Component.text(user.getName());
		Component value = Component.text((boolean)parsed[0]);
		if (user.isDeniedInTop() == denied) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_USER_TOP_SET_ALREADY.component(RED, name.color(DARK_RED), value.color(DARK_RED))));
			return;
		}
		CompletableFuture<Exception> future = user.setDeniedInTop(denied);
		try {
			Exception ex = future.get();
			if (ex != null) throw ex;
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_USER_TOP_SET_SUCCESS.component(LIGHT_PURPLE, name.color(AQUA), value.color(AQUA))));
		} catch (Exception e) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_EXECUTION.component(DARK_RED)));
			e.printStackTrace();
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_TOP_SET_DESCRIPTION;
	}

}
