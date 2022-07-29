package me.szumielxd.portfel.proxy.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.github.curiousoddman.rgxgen.RgxGen;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB.DateCondition;
import net.kyori.adventure.text.Component;

public class CreategiftcodeCommand extends SimpleCommand {
	
	public final List<CmdArg> args = Arrays.asList(
			// order
			new CmdArg(LangKey.COMMAND_ARGTYPES_GIFTORDER_DISPLAY, LangKey.COMMAND_ARGTYPES_GIFTORDER_DESCRIPTION, LangKey.EMPTY, s -> s, s -> new ArrayList<>()),
			// expiration
			new CmdArg(LangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_DISPLAY, LangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_DESCRIPTION, LangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_ERROR, str -> {try {return DateCondition.parseTime(str);} catch (NumberFormatException e) {return null;}}, (s,args) -> {
				String last = args[args.length-1];
				List<String> list = new ArrayList<>();
				try {Stream.of("s", "m", "h", "d", "mo", "y").map(u -> Long.parseLong(last)+u).forEach(list::add);} catch (NumberFormatException e) {}
				list.add(String.valueOf(System.currentTimeMillis()));
				list.add("10");
				list.add("-1");
				return list;
			}),
			// servers
			new CmdArg(LangKey.COMMAND_ARGTYPES_GIFTSERVERS_DISPLAY, LangKey.COMMAND_ARGTYPES_GIFTSERVERS_DESCRIPTION, LangKey.EMPTY, s -> s, s -> new ArrayList<>()),
			// token
			new CmdArg(LangKey.COMMAND_ARGTYPES_GIFTTOKEN_DISPLAY, LangKey.COMMAND_ARGTYPES_GIFTTOKEN_DESCRIPTION, null, s -> s, s -> new ArrayList<>())
	);

	public CreategiftcodeCommand(@NotNull PortfelProxyImpl plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "creategiftcode", "creategift", "createcode");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed == null) return;
		PortfelProxyImpl pl = (PortfelProxyImpl)this.getPlugin();
		String order = (String)parsed[0];
		long expiration = (long)parsed[1];
		String servers = (String)parsed[2];
		String token = (String)parsed[3];
		if (token == null) token = new RgxGen("[a-zA-Z0-9]{12}").generate();
		if (expiration <= System.currentTimeMillis()) {
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_CREATEGIFTCODE_PAST.component(RED)));
			return;
		}
		try {
			if (pl.getTokenDatabase().getToken(token) != null) {
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_CREATEGIFTCODE_ALREADY.component(RED, Component.text(token, AQUA))));
				return;
			}
			pl.getTokenDatabase().registerToken(token, servers, order, ProxyActionExecutor.sender(sender), expiration);
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_CREATEGIFTCODE_SUCCESS.component(GREEN, Component.text(token, AQUA), Component.text(order, AQUA), Component.text(servers, AQUA), Component.text(expiration, AQUA))));
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_CREATEGIFTCODE_FAIL.component(RED, Component.text(token, AQUA))));
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_CREATEGIFTCODE_DESCRIPTION;
	}

}
