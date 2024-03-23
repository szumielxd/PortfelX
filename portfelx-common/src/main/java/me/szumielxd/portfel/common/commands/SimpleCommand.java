package me.szumielxd.portfel.common.commands;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

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
		List<CmdArg> origin = this.getArgs();
		List<CmdArg> flyingArgs = new ArrayList<>();
		List<CmdArg> argList = new ArrayList<>();
		for (int i = 0; i < origin.size(); i++) {
			CmdArg arg = origin.get(i);
			if (arg.isFlying()) flyingArgs.add(arg);
			else argList.add(arg);
		}
		List<String> completions = new ArrayList<>();
		if (args.length > 0 && args.length <= origin.size()) {
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
	
	protected @Nullable Object[] validateArgs(CommonSender<C> sender, String... args) {
		Map<CmdArg, Integer> flyingArgs = new HashMap<>();
		List<CmdArg> origin = this.getArgs();
		for (int i = 0; i < origin.size(); i++) {
			CmdArg arg = origin.get(i);
			if (arg.isFlying()) flyingArgs.put(arg, i);
		}
		int index = 0;
		Object[] arr = new Object[origin.size()];
		
		// loop through all command arguments
		for (int i = 0; i < origin.size(); i++) {
			CmdArg arg = origin.get(i);
			if (args.length > index && flyingArgs.size() > 0) {
				/* checking for flying arguments */
				flyCheck:
				for (CmdArg fly : flyingArgs.keySet()) {
					if (args[index].toLowerCase().startsWith(fly.getPrefix().toLowerCase())) {
						Object obj = fly.parseArg(args[index++]);
						arr[flyingArgs.get(fly)] = obj;
						break flyCheck;
					}
				}
				/* end checking */
			}
			if (arg.isFlying()) continue; // ignore flying arguments
			Object obj = args.length > index ? arg.parseArg(args[index]) : null; // return null when array out of bound or when parse result is null
			arr[i] = obj;
			if (obj == null) {
				if (!arg.isOptional()) {
					if (args.length > index) sender.sendTranslated(MiscUtils.PREFIX.append(arg.getArgError(Component.text(args[index], DARK_RED))));
					else sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
					return null;
				}
			} else {
				index++;
			}
		}
		if (index < args.length) { // check for bigger amount of given arguments
			sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
			return null;
		}
		return arr;
	}

}
