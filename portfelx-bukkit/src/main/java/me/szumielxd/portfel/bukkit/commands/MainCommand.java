package me.szumielxd.portfel.bukkit.commands;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;

public class MainCommand implements AbstractCommand, TabExecutor {

	private final PortfelBukkitImpl plugin;
	private final PluginCommand command;
	private Map<String, SimpleCommand> childrens = new HashMap<>();
	private static final String HELP = "help";
	
	
	public MainCommand(@NotNull PortfelBukkitImpl plugin, @NotNull PluginCommand command) {
		this.plugin = plugin;
		this.command = command;
		this.register(
				new HelpCommand(plugin, this, HELP),
				new TestmodeCommand(plugin, this, "testmode"),
				new SystemParentCommand(plugin, this)
		);
	}
	
	
	private void register(SimpleCommand... command) {
		Arrays.asList(command).forEach(cmd -> this.childrens.putIfAbsent(cmd.getName().toLowerCase(), cmd));
		Arrays.asList(command).forEach(cmd -> Arrays.asList(cmd.getAliases()).forEach(str -> this.childrens.putIfAbsent(str, cmd)));
	}
	

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 0) args = new String[] { "" };
		SimpleCommand cmd = this.childrens.get(args[0].toLowerCase());
		if (cmd == null) cmd = this.childrens.get(HELP);
		if (!cmd.hasPermission(sender)) {
			sender.sendMessage(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_PERMISSION.component(RED)));
			return;
		} else if (!cmd.getAccess().canAccess(sender)) {
			sender.sendMessage(Portfel.PREFIX.append(cmd.getAccess().getAccessMessage().component(RED)));
			return;
		}
		cmd.onCommand(sender, parsedArgs, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
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
	public @NotNull List<CmdArg> getArgs() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.EMPTY;
	}
	
	public @NotNull List<SimpleCommand> getChildrens() {
		return this.childrens.values().stream().distinct().collect(Collectors.toList());
	}


	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return this.onTabComplete(BukkitSender.wrap(this.plugin, sender), new String[] {alias}, args);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.plugin.getTaskManager().runTaskAsynchronously(() -> this.onCommand(BukkitSender.wrap(this.plugin, sender), new Object[0], new String[] {label}, args));
		return true;
	}

	@Override
	public @NotNull String getName() {
		return this.command.getName();
	}

	@Override
	public @NotNull String[] getAliases() {
		return this.command.getAliases().toArray(new String[0]);
	}

	@Override
	public @NotNull String getPermission() {
		return this.command.getPermission();
	}

}
