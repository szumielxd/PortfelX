package me.szumielxd.portfel.proxy.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;

public class MainCommand extends CommonCommand implements AbstractCommand {

	
	private final PortfelProxyImpl plugin;
	private Map<String, SimpleCommand> childrens = new HashMap<>();
	private final String help = "help";
	
	
	public MainCommand(@NotNull PortfelProxyImpl plugin, @NotNull String name, @NotNull String permission, @NotNull String... aliases) {
		super(name, permission, aliases);
		this.plugin = plugin;
		this.register(
				new HelpCommand(plugin, this, help),
				new ListgiftcodesCommand(plugin, this),
				new CreategiftcodeCommand(plugin, this),
				new DeletegiftcodeCommand(plugin, this),
				new GiftcodeParentCommand(plugin, this),
				new SystemParentCommand(plugin, this),
				new UserParentCommand(plugin, this),
				new LogParentCommand(plugin, this)
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
		if (cmd == null) cmd = this.childrens.get(this.help);
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
	public void execute(@NotNull ProxySender sender, @NotNull String[] args) {
		this.plugin.getTaskManager().runTaskAsynchronously(() -> {
			this.onCommand(sender, new Object[0], new String[] {this.getName()}, args);
		});
	}
	
	public @NotNull List<SimpleCommand> getChildrens() {
		return this.childrens.values().stream().distinct().collect(Collectors.toList());
	}


	@Override
	public @NotNull List<String> onTabComplete(@NotNull ProxySender sender, @NotNull String[] args) {
		return this.onTabComplete(sender, new String[] {this.getName()}, args);
	}

}
