package me.szumielxd.portfel.proxy.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.HelpCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.CollectionUtils;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxySender;

public class MainCommand<C> extends CommonCommand<C> implements ParentLikeCommand<C> {

	
	private static final @NotNull String HELP = "help";
	
	private final @NotNull PortfelProxyImpl<C> plugin;
	private final @NotNull Map<String, SimpleCommand<C>> childrens = new HashMap<>();
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = MainLangKey.EMPTY;
	
	
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
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		var cmd = getChildren(parsedContext.argLeft(0)).orElseGet(() -> this.childrens.get(HELP));
		if (cmd.validateCanUse(sender)) {	
			cmd.onCommand(sender, parsedContext.skipArgsLeft(1));
		}
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
				return cmd.onTabComplete(sender, CollectionUtils.mergeArrays(label, args[0]), CollectionUtils.popArray(args));
			}
		}
		return List.of();
	}

	@Override
	public void execute(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		this.plugin.getTaskManager()
				.runTaskAsynchronously(
						() -> this.onCommand(sender, ParsedCommandContext.initial(getName(), args)));
	}
	
	@Override
	public @NotNull List<SimpleCommand<C>> getChildrens() {
		return this.childrens.values().stream()
				.distinct()
				.toList();
	}

	@Override
	public @NotNull Optional<SimpleCommand<C>> getChildren(@Nullable String name) {
		if (name == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.childrens.get(name.toLowerCase()));
	}


	@Override
	public @NotNull List<String> onTabComplete(@NotNull ProxySender<C> sender, @NotNull String[] args) {
		return this.onTabComplete(sender, new String[] {this.getName()}, args);
	}

}
