package me.szumielxd.portfel.bukkit.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.HelpCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public class MainCommand implements ParentLikeCommand<Component>, TabExecutor {

	private static final @NotNull String HELP = "help";
	
	private final @NotNull PortfelBukkitImpl plugin;
	private final @NotNull PluginCommand command;
	private final @NotNull Map<String, SimpleCommand<Component>> childrens = new HashMap<>();
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = MainLangKey.EMPTY;
	
	
	public MainCommand(@NotNull PortfelBukkitImpl plugin, @NotNull PluginCommand command) {
		this.plugin = plugin;
		this.command = command;
		this.register(List.of(
				new HelpCommand<>(plugin, this, HELP),
				new TestmodeCommand(plugin, this, "testmode"),
				new SystemParentCommand(plugin, this)));
	}
	
	
	private void register(Collection<SimpleCommand<Component>> commands) {
		commands.forEach(cmd -> this.childrens.putIfAbsent(cmd.getName().toLowerCase(), cmd));
		commands.forEach(cmd -> Arrays.asList(cmd.getAliases())
				.forEach(str -> this.childrens.putIfAbsent(str, cmd)));
	}
	

	@Override
	public void onCommand(@NotNull CommonSender<Component> sender, @NotNull ParsedCommandContext parsedContext) {
		var cmd = getChildren(parsedContext.argLeft(0)).orElseGet(() -> this.childrens.get(HELP));
		if (cmd.validateCanUse(sender)) {	
			cmd.onCommand(sender, parsedContext.skipArgsLeft(1));
		}
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender<Component> sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			return this.childrens.entrySet().stream()
					.filter(e -> e.getValue().canUse(sender))
					.map(Entry::getKey)
					.filter(s -> s.toLowerCase().startsWith(arg))
					.toList();
		} else if (args.length > 1) {
			SimpleCommand<Component> cmd = this.childrens.get(args[0].toLowerCase());
			if (cmd != null) {
				return cmd.onTabComplete(sender, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
			}
		}
		return List.of();
	}
	
	@Override
	public @NotNull List<SimpleCommand<Component>> getChildrens() {
		return this.childrens.values().stream()
				.distinct()
				.toList();
	}

	@Override
	public @NotNull Optional<SimpleCommand<Component>> getChildren(@Nullable String name) {
		if (name == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.childrens.get(name.toLowerCase()));
	}


	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return this.onTabComplete(BukkitSender.wrap(this.plugin, sender), new String[] {alias}, args);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.plugin.getTaskManager()
				.runTaskAsynchronously(
						() -> this.onCommand(BukkitSender.wrap(this.plugin, sender), ParsedCommandContext.initial(label, args)));
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
