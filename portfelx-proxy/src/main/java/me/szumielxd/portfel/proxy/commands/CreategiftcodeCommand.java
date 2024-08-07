package me.szumielxd.portfel.proxy.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.github.curiousoddman.rgxgen.RgxGen;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyActionExecutor;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB.DateCondition;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class CreategiftcodeCommand<C> extends SimpleCommand<C> {
	
	private static final @NotNull RgxGen TOKEN_GENERATOR = RgxGen.parse("[a-zA-Z0-9]{12}");
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(
			// order
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_GIFTORDER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_GIFTORDER_DESCRIPTION, MainLangKey.EMPTY, s -> s, s -> List.of()),
			// expiration
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_DESCRIPTION, ProxyLangKey.COMMAND_ARGTYPES_GIFTEXPIRATION_ERROR, str -> {try {return DateCondition.parseTime(str);} catch (NumberFormatException e) {return null;}}, (s,args) -> {
				String last = args[args.length-1];
				List<String> list = new LinkedList<>();
				try {
					long arg = Long.parseLong(last);
					Stream.of("s", "m", "h", "d", "mo", "y")
							.map(u -> arg + u)
							.forEach(list::add);
				} catch (NumberFormatException e) {
					// ignore
				}
				list.add(String.valueOf(System.currentTimeMillis()));
				list.add("10");
				list.add("-1");
				return list;
			}),
			// servers
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_GIFTSERVERS_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_GIFTSERVERS_DESCRIPTION, MainLangKey.EMPTY, s -> s, s -> List.of()),
			// token
			new CmdArg(ProxyLangKey.COMMAND_ARGTYPES_GIFTTOKEN_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_GIFTTOKEN_DESCRIPTION, null, s -> s, s -> List.of())
	);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();

	public CreategiftcodeCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "creategiftcode", "creategift", "createcode");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		this.parseArguments(sender, args).ifPresent(parsed -> {
			PortfelProxyImpl<C> pl = (PortfelProxyImpl<C>)this.getPlugin();
			String order = (String)parsed.staticArgs()[0];
			long expiration = (long)parsed.staticArgs()[1];
			String servers = (String)parsed.staticArgs()[2];
			String token = Optional.ofNullable((String)parsed.staticArgs()[3])
					.orElseGet(TOKEN_GENERATOR::generate);
			if (expiration <= System.currentTimeMillis()) {
				ProxyLangKey.COMMAND_CREATEGIFTCODE_PAST.draft()
						.sendPrefixed(sender);
				return;
			}
			try {
				if (pl.getTokenDatabase().getToken(token) != null) {
					ProxyLangKey.COMMAND_CREATEGIFTCODE_ALREADY.draft(token)
							.sendPrefixed(sender);
					return;
				}
				pl.getTokenDatabase().registerToken(token, servers, order, ProxyActionExecutor.sender(sender), expiration);
				ProxyLangKey.COMMAND_CREATEGIFTCODE_SUCCESS.draft(token, order, servers, expiration)
						.sendPrefixed(sender);
			} catch (Exception e) {
				e.printStackTrace();
				ProxyLangKey.COMMAND_CREATEGIFTCODE_FAIL.draft(token)
						.sendPrefixed(sender);
			}
		});
	}

	@Override
	public @NotNull LangKey getDescription() {
		return ProxyLangKey.COMMAND_CREATEGIFTCODE_DESCRIPTION;
	}

}
