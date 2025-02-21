package me.szumielxd.portfel.proxy.commands.user;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class UserInfoCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_INFO_DESCRIPTION;
	

	public UserInfoCommand(@NotNull Portfel<C> plugin, @NotNull AbstractCommand<C> parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		User user = (User) parsedContext.parsedArgs().staticArgs()[0];
		String[] label = parsedContext.label();
		String commandPrefix = String.join(" ", Arrays.copyOf(label, 2)) + " ";
		String parentCommand = String.join(" ", Arrays.copyOf(label, label.length - 1)) + " ";
		String id = user.getUniqueId() != null? user.getUniqueId().toString() : "null";
		MessageDraft header = ProxyLangKey.COMMAND_USER_INFO_HEADER
				.draft(interactiveValue(user.getName(), commandPrefix));
		MessageDraft uuid = ProxyLangKey.COMMAND_USER_INFO_UUID
				.draft(interactiveValue(id, commandPrefix));
		MessageDraft uuidType = ProxyLangKey.COMMAND_USER_INFO_UUIDTYPE
				.draft(MessageDraft.uuidType(user.getUniqueId()));
		MessageDraft status = ProxyLangKey.COMMAND_USER_INFO_STATUS
				.draft(MessageDraft.onlineStatus(user.isOnline()));
		MessageDraft userdata = ProxyLangKey.COMMAND_USER_INFO_USERDATA
				.draft();
		MessageDraft balance = ProxyLangKey.COMMAND_USER_INFO_BALANCE
				.draft(user.getBalance(), parentCommand + "eco");
		MessageDraft minorBalance = ProxyLangKey.COMMAND_USER_INFO_MINORBALANCE
				.draft(user.getBalance(), parentCommand + "meco");
		MessageDraft inTop = ProxyLangKey.COMMAND_USER_INFO_INTOP
				.draft(MessageDraft.trueFalse(!user.isDeniedInTop()));
		
		Stream.of(header, uuid, uuidType, status, userdata, balance, minorBalance, inTop)
				.map(MessageDraft::prefixed)
				.collect(MessageDraft.join(MessageDraft.newline()))
				.send(sender);
	}
	
	private @NotNull MessageDraft interactiveValue(@NotNull String value, @NotNull String commandPrefix) {
		return ProxyLangKey.COMMAND_USER_INFO_VALUE
				.draft(value, commandPrefix, ProxyLangKey.COMMAND_USER_INFO_VALUEHOVER);
	}

}