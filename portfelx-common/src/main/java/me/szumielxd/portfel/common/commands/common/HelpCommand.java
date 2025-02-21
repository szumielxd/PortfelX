package me.szumielxd.portfel.common.commands.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.ParentCommand;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;

public class HelpCommand<C> extends SimpleCommand<C> {
	

	public HelpCommand(@NotNull Portfel<C> plugin, @NotNull ParentLikeCommand<C> parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		String[] label = parsedContext.label();
		String[] shortLabel = Arrays.copyOf(label, label.length - 1);
		Portfel<C> pl = this.getPlugin();
		MainLangKey.COMMAND_MAIN_RUNNING
				.draft(pl.toString())
				.sendPrefixed(sender);
		if (!label[shortLabel.length].equals("")) {
			ParentCommand<C> parent = (ParentCommand<C>) this.getParent();
			parent.getChildrens().stream()
					.sorted(Comparator.comparing(SimpleCommand::getName, String.CASE_INSENSITIVE_ORDER))
					.forEachOrdered(
							cmd -> MainLangKey.COMMAND_MAIN_SUBCOMMANDS_LINE
									.draft(
											"/" + String.join(" ", shortLabel) + " " + cmd.getName(),
											cmd.getAllArgs().stream()
													.map(CmdArg::asDraft)
													.map(MessageDraft.space()::append)
													.collect(MessageDraft.joinFlattened()),
											MessageDraft.array(
													MainLangKey.COMMAND_SUBCOMMANDS_EXECUTE,
													MessageDraft.newline(),
													MainLangKey.COMMAND_SUBCOMMANDS_INSERT)
											)
									.send(sender));
		} else {
			MainLangKey.COMMAND_MAIN_USE
					.draft(
							"/" + String.join(" ", shortLabel) + " help",
							MessageDraft.array(
									MainLangKey.COMMAND_SUBCOMMANDS_EXECUTE,
									MessageDraft.newline(),
									MainLangKey.COMMAND_SUBCOMMANDS_INSERT))
					.sendPrefixed(sender);
		}
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
		return MainLangKey.COMMAND_HELP_DESCRIPTION;
	}

	
	
	
	
}
