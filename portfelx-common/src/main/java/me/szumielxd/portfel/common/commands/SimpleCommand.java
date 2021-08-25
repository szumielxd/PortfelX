package me.szumielxd.portfel.common.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public abstract class SimpleCommand implements AbstractCommand {
	
	
	protected final List<String> emptyList = Collections.unmodifiableList(Collections.emptyList());
	protected final List<CmdArg> emptyArgList = Collections.unmodifiableList(Collections.emptyList());
	private final Portfel plugin;
	private final AbstractCommand parent;
	private final String name;
	private final String permission;
	private final String[] aliases;
	
	
	public SimpleCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent, @NotNull String name, @NotNull String... aliases) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
		this.permission = parent.getPermission() + "." + this.name;
		this.parent = parent;
	}
	
	
	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
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
			return completions.stream().filter(s -> s.toLowerCase().startsWith(arg)).sorted().collect(Collectors.toList());
		}
		return this.emptyList;
	}
	

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public @NotNull String[] getAliases() {
		return this.aliases.clone();
	}

	@Override
	public @NotNull String getPermission() {
		return this.permission;
	}
	
	public @NotNull AbstractCommand getParent() {
		return this.parent;
	}
	
	protected @NotNull Portfel getPlugin() {
		return this.plugin;
	}
	
	protected @Nullable Object[] validateArgs(CommonSender sender, String... args) {
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
					if (args.length > index) sender.sendTranslated(Portfel.PREFIX.append(arg.getArgError(Component.text(args[index], DARK_RED))));
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
