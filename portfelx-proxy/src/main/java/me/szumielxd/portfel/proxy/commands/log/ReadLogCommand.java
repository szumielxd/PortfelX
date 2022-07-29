package me.szumielxd.portfel.proxy.commands.log;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import me.szumielxd.portfel.proxy.commands.CommonArgs;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.ActionType;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.LogEntry;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger.NumericCondition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class ReadLogCommand extends SimpleCommand {
	
	private List<String> signs = Arrays.asList("!", "<", ">");
	private Pattern numCond = Pattern.compile("\\d+");
	private Pattern extNumCond = Pattern.compile("[<>!]?\\d+(-\\d+)?");
	
	private List<CmdArg> args = Arrays.asList(
			CommonArgs.PAGENUMBER,
			CommonArgs.PAGESIZE,
			new CmdArg(true, "targets=", LangKey.COMMAND_ARGTYPES_LOGTARGET_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGTARGET_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "targets=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes).map(String::toLowerCase).collect(Collectors.toList());
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = this.getPlugin().getUserManager().getLoadedUsers().stream().map(User::getName).filter(str -> !prefixesLower.contains(str)).map(prefix::concat).collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "executors=", LangKey.COMMAND_ARGTYPES_LOGEXECUTOR_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGEXECUTOR_DESCRIPTION, null, str -> str.split(","), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "executors=";
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
			new CmdArg(true, "servers=", LangKey.COMMAND_ARGTYPES_LOGSERVER_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGSERVER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "orders=", LangKey.COMMAND_ARGTYPES_LOGORDER_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGORDER_DESCRIPTION, null, str -> str.split(","), (s, arr) -> Arrays.asList(arr[arr.length-1]+",")),
			new CmdArg(true, "actions=", LangKey.COMMAND_ARGTYPES_LOGACTION_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGACTION_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(ActionType::parse).filter(Objects::nonNull).toArray(ActionType[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "actions=";
				String[] elements = arg.substring(prefix.length()).split(",", -1);
				String[] prefixes = Arrays.copyOf(elements, elements.length-1);
				List<String> prefixesLower = Stream.of(prefixes).map(String::toLowerCase).collect(Collectors.toList());
				prefix += String.join(",", prefixes);
				arg = elements[elements.length-1];
				if (!prefix.isEmpty()) prefix += ",";
				List<String> list = Stream.of(ActionType.values()).map(ActionType::name).filter(str -> !prefixesLower.contains(str)).map(prefix::concat).collect(Collectors.toList());
				list.add(prefix+",");
				return list;
			}),
			new CmdArg(true, "values=", LangKey.COMMAND_ARGTYPES_LOGVALCOND_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGVALCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(NumericCondition::parse).filter(Optional::isPresent).map(Optional::get).toArray(NumericCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "values=";
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
			new CmdArg(true, "balances=", LangKey.COMMAND_ARGTYPES_LOGBALCOND_DISPLAY, LangKey.COMMAND_ARGTYPES_LOGBALCOND_DESCRIPTION, null, str -> Stream.of(str.split(",")).map(NumericCondition::parse).filter(Optional::isPresent).map(Optional::get).toArray(NumericCondition[]::new), (s, arr) -> {
				String arg = arr[arr.length-1];
				String prefix = "balances=";
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
	

	public ReadLogCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "read", "get");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		Object[] parsed = this.validateArgs(sender, args);
		if (parsed != null) {
			try {
				List<LogEntry> logs = ((PortfelProxyImpl)this.getPlugin()).getTransactionLogger().getLogs((String[])parsed[2], (String[])parsed[3], (String[])parsed[4], (String[])parsed[5], (ActionType[])parsed[6], (NumericCondition[])parsed[7], (NumericCondition[])parsed[8]);
				
				int size = parsed[1] != null ? (int)parsed[1] : 5;
				int maxPage = (int) Math.ceil(logs.size()/(double)size);
				int page = parsed[0] != null ? (int)parsed[0] : maxPage;
				
				int offset = Math.max(0, (page-1)*size);
				if (offset >= logs.size()) {
					page = 1;
					offset = 0;
				}
				
				Component header = Portfel.PREFIX.append(LangKey.COMMAND_LOG_READ_HEADER.component(DARK_PURPLE)).append(Component.text(" (", GRAY).append(
						LangKey.COMMAND_LOG_READ_PAGE.component(Component.text(page, WHITE), Component.text(maxPage, WHITE))).append(Component.text(")")));
				sender.sendTranslated(header);
				Lang lang = Lang.get(sender);
				for (int i = 0; i < size; i++) {
					if (i + offset < logs.size()) {
						LogEntry log = logs.get(i + offset);
						Component exec = this.prepareInteractive(Component.text(log.getExecutor().getDisplayName() + "@" + log.getServer(), GREEN), log.getExecutor().getDisplayName(), log.getExecutor().getUniqueId());
						Component target = this.prepareInteractive(Component.text(log.getTargetName(), AQUA), log.getTargetName(), log.getTargetUniqueId());
						Component valComp = Component.text(log.getType().format(String.valueOf(log.getValue())), log.getType().getColor()).hoverEvent(Component.text(log.getType().format(String.valueOf(log.getValue())), log.getType().getColor())
								.append(Component.newline()).append(LangKey.LOG_VALUE_ACTION.component(GRAY, Component.text(log.getType().name(), AQUA))).append(Component.newline())
								.append(LangKey.LOG_VALUE_OLD_BALANCE.component(GRAY, Component.text(log.getBalance(), AQUA))));
						Component logLine = Portfel.PREFIX.append(Component.text("#"+log.getLogId(), LIGHT_PURPLE)).append(Component.text(" (", DARK_GRAY))
								.append(LangKey.COMMAND_LOG_READ_TIME_AGO.component(GRAY, Component.text(MiscUtils.formatDuration(lang, System.currentTimeMillis()-log.getTime().getTime(), true))))
								.append(Component.text(") (", DARK_GRAY)).append(exec).append(Component.text(") [", DARK_GRAY)).append(target).append(Component.text("]", DARK_GRAY)).append(Component.newline())
								.append(Portfel.PREFIX).append(Component.text("> ", GRAY)).append(Component.text(log.getOrderName(), WHITE).hoverEvent(Component.text(log.getOrderName(), WHITE).append(Component.newline())
										.append(LangKey.LOG_VALUE_DATE.component(GRAY, Component.text(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(log.getTime().getTime())), AQUA))))).append(Component.space()).append(valComp);
						sender.sendTranslated(logLine);
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
		return LangKey.COMMAND_LOG_READ_DESCRIPTION;
	}
	
	
	private @Nullable Integer tryParseUnsignedNotZeroInt(String text) {
		try {
			int i = Integer.parseInt(text);
			return i > 0 ? i : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private @NotNull Component prepareInteractive(@NotNull Component comp, @NotNull String name, @NotNull UUID uuid) {
		return comp.hoverEvent(Component.text(uuid.toString(), AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.LOG_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.LOG_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand(name)).insertion(uuid.toString());
	}

}
