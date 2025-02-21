package me.szumielxd.portfel.proxy.commands.log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.ActionType;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.LogEntry;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.NumericCondition;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class ReadLogCommand<C> extends SimpleCommand<C> {
	
	private static final List<String> SIGNS = List.of("!", "<", ">");
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");
	private static final Pattern EXTENDED_NUMERIC_PATTERN = Pattern.compile("[<>!]?\\d+(-\\d+)?");
	
	@Getter private final List<CmdArg> staticArgs = List.of(
				CommonArgs.PAGENUMBER
			);
	@Getter private final List<CmdArg> flyingArgs = List.of(
			CommonArgs.PAGESIZE,
			new CmdArg(true, "targets=", ProxyLangKey.COMMAND_ARGTYPES_LOGTARGET_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGTARGET_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "targets=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes)
						.map(String::toLowerCase)
						.toList();
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = this.getPlugin().getUserManager().getLoadedUsers().stream().map(User::getName).filter(str -> !prefixesLower.contains(str)).map(prefix::concat).collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "executors=", ProxyLangKey.COMMAND_ARGTYPES_LOGEXECUTOR_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGEXECUTOR_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "executors=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes)
						.map(String::toLowerCase)
						.toList();
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = Stream.concat(this.getPlugin().getUserManager().getLoadedUsers().stream().map(User::getName), Stream.of("Console")).filter(str -> !prefixesLower.contains(str)).map(prefix::concat).collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "servers=", ProxyLangKey.COMMAND_ARGTYPES_LOGSERVER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGSERVER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "orders=", ProxyLangKey.COMMAND_ARGTYPES_LOGORDER_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGORDER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "actions=", ProxyLangKey.COMMAND_ARGTYPES_LOGACTION_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGACTION_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(ActionType::parse).filter(Objects::nonNull).toArray(ActionType[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "actions=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes)
						.map(String::toLowerCase)
						.toList();
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = Stream.of(ActionType.values())
						.map(ActionType::name)
						.filter(str -> !prefixesLower.contains(str))
						.map(prefix::concat)
						.toList();
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "values=", ProxyLangKey.COMMAND_ARGTYPES_LOGVALCOND_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGVALCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(NumericCondition::parse).filter(Optional::isPresent).map(Optional::get).toArray(NumericCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "values=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new ArrayList<>();
				if (arg.isEmpty()) list.addAll(SIGNS);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (NUMERIC_PATTERN.matcher(arg).matches()) list.add("-");
				if (EXTENDED_NUMERIC_PATTERN.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			}),
			new CmdArg(true, "balances=", ProxyLangKey.COMMAND_ARGTYPES_LOGBALCOND_DISPLAY, ProxyLangKey.COMMAND_ARGTYPES_LOGBALCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(NumericCondition::parse).filter(Optional::isPresent).map(Optional::get).toArray(NumericCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "balances=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = new LinkedList<>();
				if (arg.isEmpty()) list.addAll(SIGNS);
				list.addAll(CommonArgs.NUMBERS_LIST);
				if (NUMERIC_PATTERN.matcher(arg).matches()) list.add("-");
				if (EXTENDED_NUMERIC_PATTERN.matcher(arg).matches()) list.add(",");
				list.replaceAll(prefix::concat);
				return list;
			})
	);
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_LOG_READ_DESCRIPTION;
	
	

	public ReadLogCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "read", "get");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		this.parseArguments(sender, parsedContext.argsLeft()).ifPresent(newParsedContext -> {
			try {
				var parsed = newParsedContext.parsedArgs();
				List<LogEntry> logs = ((PortfelProxyImpl<C>)this.getPlugin()).getTransactionLogger()
						.getLogs(
								(String[]) parsed.flyingArgs()[1],
								(String[]) parsed.flyingArgs()[2],
								(String[]) parsed.flyingArgs()[3],
								(String[]) parsed.flyingArgs()[4],
								(ActionType[]) parsed.flyingArgs()[5],
								(NumericCondition[]) parsed.flyingArgs()[6],
								(NumericCondition[]) parsed.flyingArgs()[7]);
				
				int size = Optional.ofNullable((int) parsed.flyingArgs()[0]).orElse(5);
				int maxPage = (int) Math.ceil(logs.size() / (double) size);
				int page = Optional.ofNullable((int) parsed.staticArgs()[0]).orElse(maxPage);
				
				int offset = Math.max(0, (page-1)*size);
				if (offset >= logs.size()) {
					page = 1;
					offset = 0;
				}
				
				ProxyLangKey.COMMAND_LOG_READ_HEADER.draft(page, maxPage)
						.sendPrefixed(sender);
				Lang lang = Lang.get(sender);
				for (int i = 0; i < size; i++) {
					if (i + offset < logs.size()) {
						LogEntry log = logs.get(i + offset);
						ProxyLangKey.COMMAND_LOG_READ_LINE1.draft(
								"#" + log.getLogId(),
								MiscUtils.formatDuration(lang, System.currentTimeMillis() - log.getTime().getTime(), true),
								ProxyLangKey.LOG_MESSAGE_LINE1.draft(
										prepareInteractive(MessageDraft.plain(log.getExecutor().getDisplayName() + "@" + log.getServer()), log.getExecutor().getDisplayName(), log.getExecutor().getUniqueId()),
										prepareInteractive(MessageDraft.plain(log.getTargetName()), log.getTargetName(), log.getTargetUniqueId())))
								.sendPrefixed(sender);
						ProxyLangKey.LOG_MESSAGE_LINE2.draft(
								ProxyLangKey.LOG_ACTION_AMOUNT.draft(
										log.getType().draft(log.getValue()),
										log.getType().name(),
										log.getValue()),
								ProxyLangKey.LOG_ACTION_NAME.draft(
										log.getOrderName(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(log.getTime().getTime()))))
								.sendPrefixed(sender);
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	
	private @Nullable Integer tryParseUnsignedNotZeroInt(String text) {
		try {
			int i = Integer.parseInt(text);
			return i > 0 ? i : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private @NotNull MessageDraft prepareInteractive(@NotNull MessageDraft draft, @NotNull String name, @NotNull UUID uuid) {
		return ProxyLangKey.LOG_USER_INTERACTIVE.draft(draft, name, uuid);
	}

}
