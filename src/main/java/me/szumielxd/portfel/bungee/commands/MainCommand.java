package me.szumielxd.portfel.bungee.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungee;
import me.szumielxd.portfel.bungee.objects.BungeeSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.objects.CmdArg;
import me.szumielxd.portfel.common.objects.CommonSender;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MainCommand extends Command implements TabExecutor, AbstractCommand {

	
	private final PortfelBungee plugin;
	private Map<String, SimpleCommand> childrens = new HashMap<>();
	private final String help = "help";
	
	
	public MainCommand(@NotNull PortfelBungee plugin, @NotNull String name, @NotNull String permission, @NotNull String... aliases) {
		super(name, permission, aliases);
		this.plugin = plugin;
		this.register(new HelpCommand(plugin, this, "help"));
	}
	
	
	private void register(SimpleCommand... command) {
		Arrays.asList(command).forEach(cmd -> this.childrens.putIfAbsent(cmd.getName().toLowerCase(), cmd));
		Arrays.asList(command).forEach(cmd -> Arrays.asList(cmd.getAliases()).forEach(str -> this.childrens.putIfAbsent(str, cmd)));
	}
	

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 0) args = new String[] { this.help };
		SimpleCommand cmd = this.childrens.get(args[0].toLowerCase());
		if (cmd == null) cmd = this.childrens.get(this.help);
		cmd.onCommand(sender, parsedArgs, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
		return;
	}

	@Override
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			return this.childrens.entrySet().stream().filter(e -> e.getValue().hasPermission(sender))
					.map(Entry::getKey).filter(s -> s.toLowerCase().startsWith(arg)).collect(Collectors.toList());
		} else if (args.length > 1) {
			SimpleCommand cmd = this.childrens.get(args[0].toLowerCase());
			if (cmd != null) return cmd.onTabComplete(sender, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
		}
		return new ArrayList<>();
	}

	@Override
	public @NotNull String[] getAliases() {
		return super.getAliases();
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.EMPTY;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		this.onCommand(new BungeeSender(this.plugin, sender), new Object[0], new String[] {this.getName()}, args);
		
	}
	
	public @NotNull List<SimpleCommand> getChildrens() {
		return this.childrens.values().stream().distinct().collect(Collectors.toList());
	}


	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return this.onTabComplete(new BungeeSender(this.plugin, sender), new String[] {this.getName()}, args);
	}

}
