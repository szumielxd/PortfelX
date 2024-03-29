package me.szumielxd.portfel.common.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;

public abstract class ParentCommand extends SimpleCommand {

	
	private HashMap<String, SimpleCommand> childrens;
	
	
	public ParentCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}
	
	protected void register(@NotNull SimpleCommand... childrens) {
		HashMap<String, SimpleCommand> childs = new HashMap<>();
		for (final SimpleCommand cmd : childrens) {
			childs.putIfAbsent(cmd.getName().toLowerCase(), cmd);
		}
		for (final SimpleCommand cmd : childrens) {
			Arrays.asList(cmd.getAliases()).forEach(str -> childs.putIfAbsent(str.toLowerCase(), cmd));
		}
		this.childrens = childs;
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		final List<CmdArg> cmdArgs = this.getArgs();
		int offset = 0;
		Object[] newParsedArgs = new Object[0];
		if (args.length > offset) {
			for (CmdArg arg : cmdArgs) {
				Object obj = arg.parseArg(args[offset]);
				if (obj != null) {
					offset++;
				} else if (!arg.isOptional()) {
					Component comp = Portfel.PREFIX.append(arg.getArgError(Component.text(args[offset], DARK_RED)));
					sender.sendTranslated(comp);
					return;
				}
				newParsedArgs = MiscUtils.mergeArrays(newParsedArgs, obj);
			}
		}
		if (args.length > offset) {
			SimpleCommand cmd = this.childrens.get(args[offset].toLowerCase());
			if (cmd != null) {
				if (!cmd.hasPermission(sender)) {
					sender.sendMessage(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_PERMISSION.component(RED)));
					return;
				} else if (!cmd.getAccess().canAccess(sender)) {
					sender.sendMessage(Portfel.PREFIX.append(cmd.getAccess().getAccessMessage().component(RED)));
					return;
				}
				cmd.onCommand(sender, MiscUtils.mergeArrays(parsedArgs, newParsedArgs), MiscUtils.mergeArrays(label, Arrays.copyOf(args, ++offset)), MiscUtils.popArray(args, offset));
				return;
			}
		}
		List<String> suggestCmd = new ArrayList<>(Arrays.asList(label.clone()));
		List<Component> fullLabel = suggestCmd.stream().map(Component::text).collect(Collectors.toList());
		offset = 0;
		for (CmdArg arg : cmdArgs) {
			if (offset < args.length) {
				if (arg.isValid(args[offset])) {
					suggestCmd.add(args[offset]);
					fullLabel.add(Component.text(args[offset++]));
				} else if (!arg.isOptional()) {
					offset++;
					fullLabel.add(Component.text("<", GRAY).append(arg.getDisplay().component(GRAY)).append(Component.text(">", GRAY)));
				}
			} else {
				if (arg.isOptional()) {
					fullLabel.add(Component.text("[<", GRAY).append(arg.getDisplay().component(GRAY)).append(Component.text(">]", GRAY)));
				} else {
					fullLabel.add(Component.text("<", GRAY).append(arg.getDisplay().component(GRAY)).append(Component.text(">", GRAY)));
				}
			}
		}
		Lang lang = Lang.get(sender);
		Component comp = Portfel.PREFIX.append(LangKey.COMMAND_SUBCOMMANDS_TITLE
				.component(LIGHT_PURPLE,Component.text(label[label.length-1], LIGHT_PURPLE)))
				.append(Component.text(" (/", GRAY).children(MiscUtils.join(" ", fullLabel).children()).append(Component.text("...)")));
		sender.sendTranslated(comp);
		Component linePrefix = Component.empty().append(Component.text("> ", LIGHT_PURPLE, BOLD));
		this.getChildrens().stream().sorted((a,b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName())).forEachOrdered(cmd -> {
			if (!cmd.hasPermission(sender) || !cmd.getAccess().canAccess(sender)) return;
			Component line = linePrefix.append(Component.text(cmd.getName(), AQUA)).append(Component.text(cmd.getArgs().isEmpty()? "" : " - ", DARK_PURPLE))
					.append(MiscUtils.join(" ", cmd.getArgs().stream().map(MiscUtils::argToComponent).toArray(Component[]::new)));
			String cmdUsage = "/" + String.join(" ", suggestCmd) + " " + cmd.getName() + String.join(" ", cmd.getArgs().stream()
					.map(arg -> MiscUtils.argToCleanText(lang, arg)).toArray(String[]::new));
			sender.sendTranslated(MiscUtils.buildCommandUsage(line, cmdUsage, cmd));
		});
		
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args) {
		List<CmdArg> subCmds = this.getArgs();
		String lastArg = args[args.length-1].toLowerCase();
		if (subCmds.size() >= args.length) {
			return subCmds.get(args.length-1).getTabCompletions(sender).stream().filter(s -> s.toLowerCase().startsWith(lastArg)).collect(Collectors.toList());
		} else if (args.length == subCmds.size() + 1) {
			List<String> list = new ArrayList<>();
			this.childrens.forEach((name, cmd) -> {
				if (cmd.hasPermission(sender) && cmd.getAccess().canAccess(sender) && name.toLowerCase().startsWith(lastArg)) list.add(name);
			});
			list.sort(String.CASE_INSENSITIVE_ORDER);
			return list;
		} else {
			String str = args[subCmds.size()];
			SimpleCommand cmd = this.childrens.get(str.toLowerCase());
			if (cmd != null && cmd.hasPermission(sender) && cmd.getAccess().canAccess(sender)) {
				return cmd.onTabComplete(sender, MiscUtils.mergeArrays(label, Arrays.copyOf(args, subCmds.size()+1)), MiscUtils.popArray(args, subCmds.size()+1));
			}
		}
		return new ArrayList<>();
	}
	
	public @NotNull Collection<SimpleCommand> getChildrens() {
		return this.childrens.values().stream().distinct().collect(Collectors.toList());
	}
	

}
