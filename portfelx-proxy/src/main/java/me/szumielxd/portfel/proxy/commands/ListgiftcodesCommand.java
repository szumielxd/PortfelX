package me.szumielxd.portfel.proxy.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB.DateCondition;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import me.szumielxd.portfel.proxy.objects.PrizeToken.ServerSelectorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class ListgiftcodesCommand extends SimpleCommand {
	
	private List<String> signs = Arrays.asList("!", "<", ">");
	private Pattern numCond = Pattern.compile("\\d+");
	private Pattern extNumCond = Pattern.compile("[<>!]?\\d+(-\\d+)?");
	
	private List<CmdArg> args = Arrays.asList(
			CommonArgs.PAGENUMBER,
			CommonArgs.PAGESIZE,
			new CmdArg(true, "servers=", LangKey.COMMAND_ARGTYPES_TOKENSERVER_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKENSERVER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "orders=", LangKey.COMMAND_ARGTYPES_TOKENORDER_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKENORDER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "creators=", LangKey.COMMAND_ARGTYPES_TOKENCREATOR_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKENCREATOR_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "creators=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes).map(String::toLowerCase).collect(Collectors.toList());
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = Stream.concat(this.getPlugin().getUserManager().getLoadedUsers().stream().map(User::getName), Stream.of("Console")).filter(str -> !prefixesLower.contains(str)).map(prefix::concat).collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "creationdates=", LangKey.COMMAND_ARGTYPES_TOKENCREATECOND_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKENCREATECOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(DateCondition::parse).filter(o -> o.isPresent()).map(Optional::get).toArray(DateCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "creationdates=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new ArrayList<>();
				if (arg.isEmpty()) list.addAll(this.signs);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (this.numCond.matcher(arg).matches()) list.add("-");
				if (this.extNumCond.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			}),
			new CmdArg(true, "expirationdates=", LangKey.COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DISPLAY, LangKey.COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(DateCondition::parse).filter(o -> o.isPresent()).map(Optional::get).toArray(DateCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "expirationdates=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new ArrayList<>();
				if (arg.isEmpty()) list.addAll(this.signs);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (this.numCond.matcher(arg).matches()) list.add("-");
				if (this.extNumCond.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			})
	);
	

	public ListgiftcodesCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "listgiftcodes", "listtokens");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			try {
				List<PrizeToken> tokens = ((PortfelProxyImpl)this.getPlugin()).getTokenDatabase().getTokens((String[])parsed[2], (String[])parsed[3], (String[])parsed[4], (DateCondition[])parsed[5], (DateCondition[])parsed[6]);
				
				int size = parsed[1] != null ? (int)parsed[1] : 5;
				int maxPage = (int) Math.ceil(tokens.size()/(double)size);
				int page = parsed[0] != null ? (int)parsed[0] : maxPage;
				
				int offset = Math.max(0, (page-1)*size);
				if (offset >= tokens.size()) {
					page = 1;
					offset = 0;
				}
				
				Component header = Portfel.PREFIX.append(LangKey.COMMAND_LISTGIFTCODES_HEADER.component(DARK_PURPLE)).append(Component.text(" (", GRAY).append(
						LangKey.COMMAND_LISTGIFTCODES_PAGE.component(Component.text(page, WHITE), Component.text(maxPage, WHITE))).append(Component.text(")")));
				sender.sendTranslated(header);
				Lang lang = Lang.get(sender);
				for (int i = 0; i < size; i++) {
					if (i + offset < tokens.size()) {
						PrizeToken token = tokens.get(i + offset);
						Component exec = this.prepareInteractive(Component.text(token.getCreator().getDisplayName(), GREEN), token.getCreator().getDisplayName(), token.getCreator().getUniqueId());
						Component expiration = LangKey.COMMAND_LISTGIFTCODES_EXPIRATION.component(GRAY, token.getExpiration() == -1 ? LangKey.COMMAND_LISTGIFTCODES_LIFETIME.component(RED) : Component.text("✝ " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(token.getExpiration())), AQUA));
						Component serversComp = Component.text(token.getSelectorType().name(), AQUA);
						if  (ServerSelectorType.WHITELIST.equals(token.getSelectorType())) serversComp = serversComp.append(Component.text(" [", DARK_GRAY).append(MiscUtils.join(Component.text(", ", DARK_GRAY), token.getServerNames().stream().map(s -> Component.text(s, GOLD)).collect(Collectors.toList()))).append(Component.text("]", DARK_GRAY)));
						Component tokenLine = Portfel.PREFIX.append(Component.text(token.getToken(), LIGHT_PURPLE)).append(Component.text(" (", DARK_GRAY))
								.append(LangKey.COMMAND_LISTGIFTCODES_TIME_AGO.component(GRAY, Component.text(MiscUtils.formatDuration(lang, System.currentTimeMillis()-token.getCreationDate().getTime(), true))))
								.append(Component.text(") (", DARK_GRAY)).append(exec).append(Component.text(")", DARK_GRAY))
								.append(Component.text(" [", DARK_GRAY).append(token.getExpiration() >= 0 ? Component.text("E", AQUA) : Component.text("L", RED))).append(Component.text("]", DARK_GRAY)).append(Component.newline())
								.append(Portfel.PREFIX).append(Component.text("> ", GRAY)).append(Component.text(token.getOrder(), WHITE)).append(Component.space()).append(serversComp).hoverEvent(expiration).insertion(token.getToken());
						sender.sendTranslated(tokenLine);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.args;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_LISTGIFTCODES_DESCRIPTION;
	}
	
	private @NotNull Component prepareInteractive(@NotNull Component comp, @NotNull String name, @NotNull UUID uuid) {
		return comp.hoverEvent(Component.text(uuid.toString(), AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_LISTGIFTCODES_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_LISTGIFTCODES_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand(name)).insertion(uuid.toString());
	}

}
