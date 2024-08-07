package me.szumielxd.portfel.common.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;

public abstract class SimpleCommand<C> implements AbstractCommand<C> {
	
	
	protected final List<String> emptyList = List.of();
	protected final List<CmdArg> emptyArgList = List.of();
	@Getter private final Portfel<C> plugin;
	@Getter private final AbstractCommand<C> parent;
	@Getter private final String name;
	@Getter private final String permission;
	@Getter private final String[] aliases;
	
	
	protected SimpleCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent, @NotNull String name, @NotNull String... aliases) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
		this.permission = parent.getPermission() + "." + this.name;
		this.parent = parent;
	}
	
	
	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender<C> sender, @NotNull String[] label, @NotNull String[] args) {
		List<CmdArg> flyingArgs = this.getFlyingArgs();
		List<CmdArg> argList = this.getStaticArgs();
		List<String> completions = new LinkedList<>();
		int size = argList.size() + flyingArgs.size();
		if (args.length > 0 && args.length <= size) {
			final int index = args.length-1;
			String arg = args[index].toLowerCase();
			if (args.length <= argList.size()) {
				completions.addAll(argList.get(index).getTabCompletions(sender, MiscUtils.mergeArrays(label, Arrays.copyOf(args, index+1))));
			}
			flyingArgs.stream().map(a -> {
				List<String> res = a.getTabCompletions(sender, label);
				if (res.isEmpty()) return Arrays.asList(a.getPrefix());
				return res;
			}).forEach(completions::addAll);
			return completions.stream()
					.filter(s -> s.toLowerCase().startsWith(arg))
					.sorted()
					.toList();
		}
		return this.emptyList;
	}
	
	protected Optional<ParsedArguments> parseArguments(CommonSender<C> sender, String... args) {
		CmdArg[] flyingArgs = this.getFlyingArgs().toArray(CmdArg[]::new);
		CmdArg[] staticArgs = this.getStaticArgs().toArray(CmdArg[]::new);
		Object[] flyingParsedArgs = new Object[flyingArgs.length];
		Object[] staticParsedArgs = new Object[staticArgs.length];
		int staticArgIndex = 0;
		for (var arg : args) {
			int flyingArgIndex = matchFlyingArg(flyingArgs, arg);
			if (flyingArgIndex > -1) {
				if (flyingParsedArgs[flyingArgIndex] == null) {
					flyingParsedArgs[flyingArgIndex] = flyingArgs[flyingArgIndex].parseArg(arg);
				}
			} else if (staticArgIndex < staticArgs.length) {
				staticParsedArgs[staticArgIndex] = staticArgs[staticArgIndex].parseArg(arg);
				if (staticParsedArgs[staticArgIndex] == null && !staticArgs[staticArgIndex].isOptional()) {
					staticArgs[staticArgIndex].getArgError(arg).send(sender, true);
					return Optional.empty();
				}
				staticArgIndex++;
			}
		}
		for (int i = staticArgIndex; i < staticArgs.length; i++) {
			if (!staticArgs[i].isOptional()) {
				sendCommandUsage(sender);
				return Optional.empty();
			}
		}
		return Optional.of(new ParsedArguments(staticParsedArgs, flyingParsedArgs));
	}
	
	protected void sendCommandUsage(CommonSender<C> sender) {
		var aliases = getAliases();
		var args = getAllArgs();
		MainLangKey.COMMAND_USAGE_TITLE.draft(getName()).send(sender, true);
		MainLangKey.COMMAND_USAGE_DESCRIPTION.draft(getDescription()).send(sender, true);
		if (aliases.length > 0) {
			MainLangKey.COMMAND_USAGE_ALIASES.draft().send(sender, true);
			for (var alias : aliases) {
				MainLangKey.COMMAND_USAGE_ALIAS_FORMAT.draft(alias).send(sender, true);
			}
		}
		if (!args.isEmpty()) {
			MainLangKey.COMMAND_USAGE_ARGUMENTS.draft().send(sender, true);
			for (var arg : args) {
				MainLangKey.COMMAND_USAGE_ARGUMENT_FORMAT.draft(arg.asDraft(), arg.getDescription()).send(sender, true);
			}
		}
	}
	
	private int matchFlyingArg(CmdArg[] flyingArgs, String rawArgument) {
		String arg = rawArgument.toLowerCase();
		for (int i = 0; i < flyingArgs.length; i++) {
			if (arg.equals(flyingArgs[i].getPrefix())) {
				return i;
			}
		}
		return -1;
	}
	
	protected record ParsedArguments(Object[] staticArgs, Object[] flyingArgs) {
		
		
		
	}

}
