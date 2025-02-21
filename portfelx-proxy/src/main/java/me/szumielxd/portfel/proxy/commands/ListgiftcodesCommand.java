package me.szumielxd.portfel.proxy.commands;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB.DateCondition;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.objects.PrizeToken;

public class ListgiftcodesCommand<C> extends SimpleCommand<C> {
	
	private static final List<String> SIGNS = Arrays.asList("!", "<", ">");
	private static final Pattern NUMBER_CONDITION = Pattern.compile("\\d+");
	private static final Pattern EXTENDED_NUMBER_CONDITION = Pattern.compile("[<>!]?\\d+(-\\d+)?");
	
	private final DateFormat expirationFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of(
			CommonArgs.PAGENUMBER,
			CommonArgs.PAGESIZE
	);
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of(
			new CmdArg(true, "servers=", ProxyLangKey.COMMAND_ARGTYPES_TOKENSERVER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKENSERVER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "orders=", ProxyLangKey.COMMAND_ARGTYPES_TOKENORDER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKENORDER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "creators=", ProxyLangKey.COMMAND_ARGTYPES_TOKENCREATOR_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKENCREATOR_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "creators=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes)
						.map(String::toLowerCase)
						.toList();
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = Stream.concat(
							this.getPlugin().getUserManager().getLoadedUsers().stream()
									.map(User::getName),
							Stream.of("Console"))
						.filter(str -> !prefixesLower.contains(str))
						.map(prefix::concat)
						.collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "creationdates=", ProxyLangKey.COMMAND_ARGTYPES_TOKENCREATECOND_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKENCREATECOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(DateCondition::parse).filter(o -> o.isPresent()).map(Optional::get).toArray(DateCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "creationdates=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new ArrayList<>();
				if (arg.isEmpty()) list.addAll(SIGNS);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (NUMBER_CONDITION.matcher(arg).matches()) list.add("-");
				if (EXTENDED_NUMBER_CONDITION.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			}),
			new CmdArg(true, "expirationdates=", ProxyLangKey.COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_TOKENEXPIRATIONCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(DateCondition::parse).filter(o -> o.isPresent()).map(Optional::get).toArray(DateCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "expirationdates=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new ArrayList<>();
				if (arg.isEmpty()) list.addAll(SIGNS);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (NUMBER_CONDITION.matcher(arg).matches()) list.add("-");
				if (EXTENDED_NUMBER_CONDITION.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			})
	);	
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_LISTGIFTCODES_DESCRIPTION;
	
	

	public ListgiftcodesCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "listgiftcodes", "listtokens");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		this.parseArguments(sender, parsedContext.argsLeft()).ifPresent(newParsedContext -> {
			try {
				var parsed = newParsedContext.parsedArgs();
				List<PrizeToken> tokens = ((PortfelProxyImpl<C>) this.getPlugin()).getTokenDatabase()
						.getTokens(
								(String[]) parsed.flyingArgs()[0],
								(String[]) parsed.flyingArgs()[1],
								(String[]) parsed.flyingArgs()[2],
								(DateCondition[]) parsed.flyingArgs()[3],
								(DateCondition[]) parsed.flyingArgs()[4]);
				
				int size = Optional.ofNullable((int) parsed.staticArgs()[1]).orElse(5);
				int maxPage = (int) Math.ceil(tokens.size() / (double) size);
				int page = Optional.ofNullable((int) parsed.staticArgs()[0]).orElse(maxPage);
				
				int offset = Math.max(0, (page-1) * size);
				if (offset >= tokens.size()) {
					page = 1;
					offset = 0;
				}
				
				ProxyLangKey.COMMAND_LISTGIFTCODES_HEADER.draft(page, maxPage)
						.sendPrefixed(sender);
				Lang lang = Lang.get(sender);
				tokens.stream()
						.skip(offset)
						.limit(size)
						.forEach(token -> sendTokenInfoLine(lang, sender, token));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	
	private void sendTokenInfoLine(@NotNull Lang lang, @NotNull CommonSender<C> sender, @NotNull PrizeToken token) {
		var executor = ProxyLangKey.COMMAND_LISTGIFTCODES_USER
				.draft(
						token.getCreator().getDisplayName(),
						token.getCreator().getUniqueId());
		
		var expirationTime = token.getExpiration() == -1 ?
				ProxyLangKey.COMMAND_LISTGIFTCODES_LIFETIME.draft()
				: ProxyLangKey.COMMAND_LISTGIFTCODES_ENDTIME
						.draft(expirationFormat.format(new Date(token.getExpiration())));
		
		var expiration = ProxyLangKey.COMMAND_LISTGIFTCODES_EXPIRATION
				.draft(expirationTime);

		var duration = MiscUtils.formatDuration(lang, System.currentTimeMillis()-token.getCreationDate().getTime(), true);
		
		ProxyLangKey.COMMAND_LISTGIFTCODES_ENTRY_FIRSTLINE.draft(
				expiration,
				token.getToken(),
				ProxyLangKey.COMMAND_LISTGIFTCODES_TIME_AGO.draft(duration),
				executor,
				getExpirationIndicator(token.getExpiration())
			).sendPrefixed(sender);
		
		ProxyLangKey.COMMAND_LISTGIFTCODES_ENTRY_SECONDLINE.draft(
				expiration,
				token.getOrder(),
				getServerSelector(token)
			).sendPrefixed(sender);
	}
	
	
	private ProxyLangKey getExpirationIndicator(long expiration) {
		if (expiration < 0) {
			return ProxyLangKey.COMMAND_LISTGIFTCODES_EXPIRATION_LIFETIME;
		}
		if (expiration < System.currentTimeMillis()) {
			return ProxyLangKey.COMMAND_LISTGIFTCODES_EXPIRATION_EXPIRED;
		}
		return ProxyLangKey.COMMAND_LISTGIFTCODES_EXPIRATION_EXPIRABLE;
	}
	
	private MessageDraft getServerSelector(@NotNull PrizeToken token) {
		return switch (token.getSelectorType()) {
			case ANY -> ProxyLangKey.COMMAND_LISTGIFTCODES_SERVERSELECTOR_ANY.draft();
			case REGISTERED -> ProxyLangKey.COMMAND_LISTGIFTCODES_SERVERSELECTOR_REGISTERED.draft();
			case WHITELIST -> ProxyLangKey.COMMAND_LISTGIFTCODES_SERVERSELECTOR_WHITELIST
					.draft(buildSelectorWhitelistServers(token.getServerNames()));
		};
	}
	
	private MessageDraft buildSelectorWhitelistServers(@NotNull Collection<String> servers) {
		return servers.stream()
				.map(ProxyLangKey.COMMAND_LISTGIFTCODES_SERVERSELECTOR_WHITELIST_ENTRY::draft)
				.collect(MessageDraft.join(", "));
	}
	

}
