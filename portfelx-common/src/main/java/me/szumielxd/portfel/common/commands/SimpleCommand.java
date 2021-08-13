package me.szumielxd.portfel.common.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.objects.CommonSender;
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
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		List<CmdArg> argList = this.getArgs();
		if (args.length > 0 && args.length <= argList.size()) {
			final int index = args.length-1;
			String arg = args[index].toLowerCase();
			return this.getArgs().get(index).getTabCompletions(sender, label).stream().filter(s -> s.toLowerCase().startsWith(arg)).sorted().collect(Collectors.toList());
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
		List<Object> list = new ArrayList<>();
		List<CmdArg> argList = this.getArgs();
		int index = 0;
		for (int i = 0; i < argList.size(); i++) {
			CmdArg arg = argList.get(i);
			if (args.length <= index) break;
			Object obj = arg.parseArg(args[index]);
			if (obj != null) {
				list.add(obj);
				index++;
			} else {
				if (!arg.isOptional()) {
					sender.sendTranslated(Portfel.PREFIX.append(arg.getArgError(Component.text(args[index], DARK_RED))));
					return null;
				}
			}
		}
		if (list.size() < argList.size()) {
			sender.sendTranslated(MiscUtils.extendedCommandUsage(this));
			return null;
		}
		return list.toArray();
	}

}
