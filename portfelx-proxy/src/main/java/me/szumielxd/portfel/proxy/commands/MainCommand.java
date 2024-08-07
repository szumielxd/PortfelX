package me.szumielxd.portfel.proxy.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.HelpCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;

public class MainCommand<C> extends CommonCommand<C> implements ParentLikeCommand<C> {

	
	private final PortfelProxyImpl<C> plugin;
	private Map<String, SimpleCommand<C>> childrens = new HashMap<>();
	private static final String HELP = "help";
	
	
	public MainCommand(@NotNull PortfelProxyImpl<C> plugin, @NotNull String name, @NotNull String permission, @NotNull String... aliases) {
		super(name, permission, aliases);
		this.plugin = plugin;
		this.register(List.of(
				new HelpCommand<>(plugin, this, HELP),
				new ListgiftcodesCommand<>(plugin, this),
				new CreategiftcodeCommand<>(plugin, this),
				new DeletegiftcodeCommand<>(plugin, this),
				new GiftcodeParentCommand<>(plugin, this),
				new SystemParentCommand<>(plugin, this),
				new UserParentCommand<>(plugin, this),
				new LogParentCommand<>(plugin, this)
		));
	}
	
	
	private void register(Collection<SimpleCommand<C>> commands) {
		commands.forEach(cmd -> this.childrens.putIfAbsent(cmd.getName().toLowerCase(), cmd));
		commands.forEach(cmd -> Arrays.asList(cmd.getAliases())
				.forEach(str -> this.childrens.putIfAbsent(str, cmd)));
	}
	

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 0) {
			args = new String[] { "" };
		}
		SimpleCommand<C> cmd = Optional.ofNullable(this.childrens.get(args[0].toLowerCase()))
				.orElseGet(() -> this.childrens.get(HELP));
		if (!cmd.hasPermission(sender)) {
			MainLangKey.ERROR_COMMAND_PERMISSION
					.draft()
					.send(sender, true);
			return;
		} else if (!cmd.getAccess().canAccess(sender)) {
			cmd.getAccess().getAccessMessage()
					.draft()
					.send(sender, true);
			return;
		}
		cmd.onCommand(sender, parsedArgs, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender<C> sender, @NotNull String[] label, @NotNull String[] args) {
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			return this.childrens.entrySet().stream()
					.filter(e -> e.getValue().canUse(sender))
					.map(Entry::getKey)
					.filter(s -> s.toLowerCase().startsWith(arg))
					.toList();
		} else if (args.length > 1) {
			SimpleCommand<C> cmd = this.childrens.get(args[0].toLowerCase());
			if (cmd != null) {
				return cmd.onTabComplete(sender, MiscUtils.mergeArrays(label, args[0]), MiscUtils.popArray(args));
			}
		}
		return List.of();
	}
	
	@Override
	public @NotNull List<CmdArg> getStaticArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull List<CmdArg> getFlyingArgs() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull LangKey getDescription() {
		return MainLangKey.EMPTY;
	}

	@Override
	public void execute(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		this.plugin.getTaskManager().runTaskAsynchronously(
				() -> this.onCommand(sender, new Object[0], new String[] {this.getName()}, args));
	}
	
	@Override
	public @NotNull List<SimpleCommand<C>> getChildrens() {
		return this.childrens.values().stream()
				.distinct()
				.toList();
	}


	@Override
	public @NotNull List<String> onTabComplete(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		return this.onTabComplete(sender, new String[] {this.getName()}, args);
	}

}
