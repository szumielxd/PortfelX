package me.szumielxd.portfel.common.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.MiscUtils;

public abstract class ParentCommand<C> extends SimpleCommand<C> implements ParentLikeCommand<C> {

	
	private HashMap<String, SimpleCommand<C>> childrens;
	
	
	protected ParentCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}
	
	protected void register(@NotNull Collection<SimpleCommand<C>> childrens) {
		HashMap<String, SimpleCommand<C>> childs = new HashMap<>();
		for (final SimpleCommand<C> cmd : childrens) {
			childs.putIfAbsent(cmd.getName().toLowerCase(), cmd);
		}
		for (final SimpleCommand<C> cmd : childrens) {
			Arrays.asList(cmd.getAliases()).forEach(str -> childs.putIfAbsent(str.toLowerCase(), cmd));
		}
		this.childrens = childs;
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		final List<CmdArg> cmdArgs = this.getAllArgs();
		int offset = 0;
		Object[] newParsedArgs = new Object[0];
		if (args.length > offset) {
			for (CmdArg arg : cmdArgs) {
				Object obj = arg.parseArg(args[offset]);
				if (obj != null) {
					offset++;
				} else if (!arg.isOptional()) {
					arg.getArgError(MessageDraft.plain(args[offset])).send(sender, true);
					return;
				}
				newParsedArgs = MiscUtils.mergeArrays(newParsedArgs, obj);
			}
		}
		if (args.length > offset) {
			SimpleCommand<C> cmd = this.childrens.get(args[offset].toLowerCase());
			if (cmd != null) {
				if (!cmd.hasPermission(sender)) {
					MainLangKey.ERROR_COMMAND_PERMISSION.draft().send(sender, true);
				} else if (!cmd.getAccess().canAccess(sender)) {
					cmd.getAccess().getAccessMessage().draft().send(sender, true);
				} else {
					cmd.onCommand(sender, MiscUtils.mergeArrays(parsedArgs, newParsedArgs), MiscUtils.mergeArrays(label, Arrays.copyOf(args, ++offset)), MiscUtils.popArray(args, offset));
				}
				return;
			}
		}
		sendHelpMessage(sender, label, cmdArgs, args);		
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender<C> sender, @NotNull String[] label, @NotNull String[] args) {
		List<CmdArg> cmdArgs = this.getAllArgs();
		String lastArg = args[args.length-1].toLowerCase();
		if (cmdArgs.size() >= args.length) {
			return cmdArgs.get(args.length-1).getTabCompletions(sender).stream()
					.filter(s -> s.toLowerCase().startsWith(lastArg))
					.toList();
		} else if (args.length == cmdArgs.size() + 1) {
			return this.childrens.entrySet().stream()
					.filter(e -> e.getKey().toLowerCase().startsWith(lastArg))
					.filter(e -> e.getValue().canUse(sender))
					.map(Entry::getKey)
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.toList();
		} else {
			String str = args[cmdArgs.size()];
			SimpleCommand<C> cmd = this.childrens.get(str.toLowerCase());
			if (cmd != null && cmd.canUse(sender)) {
				return cmd.onTabComplete(sender, MiscUtils.mergeArrays(label, Arrays.copyOf(args, cmdArgs.size() + 1)), MiscUtils.popArray(args, cmdArgs.size() + 1));
			}
		}
		return List.of();
	}
	
	@Override
	public @NotNull Collection<SimpleCommand<C>> getChildrens() {
		return this.childrens.values().stream()
				.distinct()
				.toList();
	}
	
	private void sendHelpMessage(@NotNull CommonSender<C> sender, String[] label, List<CmdArg> cmdArgs, String[] args) {
		List<String> suggestCmd = new ArrayList<>(Arrays.asList(label.clone()));
		var fullLabel = suggestCmd.stream()
				.map(MessageDraft::plain)
				.collect(Collectors.toCollection(LinkedList::new));
		int offset = 0;
		for (CmdArg arg : cmdArgs) {
			if (offset < args.length) {
				if (arg.isValid(args[offset])) {
					suggestCmd.add(args[offset]);
					fullLabel.add(MessageDraft.plain(args[offset++]));
				} else if (!arg.isOptional()) {
					offset++;
					fullLabel.add(arg.asDraft());
				}
			} else {
				fullLabel.add(arg.asDraft());
			}
		}
		MainLangKey.COMMAND_SUBCOMMANDS_TITLE.draft(
				label[label.length-1],
				fullLabel.stream().collect(MessageDraft.join(" ")))
				.send(sender, true);
		getChildrens().stream()
				.filter(cmd -> cmd.canUse(sender))
				.sorted(Comparator.comparing(SimpleCommand::getName, String.CASE_INSENSITIVE_ORDER))
				.forEachOrdered(cmd -> {
					var cmdArgList = cmd.getAllArgs();
					String cmdUsage = "/%s %s".formatted(String.join(" ", suggestCmd), cmd.getName()); // 2
					if (cmdArgList.isEmpty()) {
						MainLangKey.COMMAND_SUBCOMMANDS_LINE_WITHOUTARGS.draft(
								cmd.getName(),
								MessageDraft.array(
										MainLangKey.COMMAND_SUBCOMMANDS_EXECUTE,
										MessageDraft.newline(),
										MainLangKey.COMMAND_SUBCOMMANDS_INSERT),
								cmdUsage)
								.send(sender);
					} else {
						MainLangKey.COMMAND_SUBCOMMANDS_LINE_WITHOUTARGS.draft(
								cmd.getName(),
								cmdArgList.stream()
										.map(CmdArg::asDraft)
										.collect(MessageDraft.join(" ")),
								MessageDraft.array(
										MainLangKey.COMMAND_SUBCOMMANDS_EXECUTE,
										MessageDraft.newline(),
										MainLangKey.COMMAND_SUBCOMMANDS_INSERT),
								cmdUsage)
								.send(sender);
					}
				});
	}
	

}
