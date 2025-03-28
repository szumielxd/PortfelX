package me.szumielxd.portfel.proxy.commands.user.top;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.proxy.PortfelProxyImpl;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;

public class TopInfoCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_USER_TOP_INFO_DESCRIPTION;
	

	public TopInfoCommand(@NotNull Portfel<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		User user = (User) parsedContext.parsedArgs().staticArgs()[0];
		try {
			Integer pos = ((PortfelProxyImpl<C>)this.getPlugin()).getDatabase().getTopPos(user)[0];
			var inTop = ProxyLangKey.COMMAND_USER_TOP_INFO_INTOP
					.draft(user.getName());
			var inTopValue = ProxyLangKey.COMMAND_USER_TOP_INFO_INTOPVALUE
					.draft(MessageDraft.trueFalse(!user.isDeniedInTop()));
			var position = ProxyLangKey.COMMAND_USER_TOP_INFO_POSITION
					.draft(user.getName());
			var positionValue = ProxyLangKey.COMMAND_USER_TOP_INFO_POSITIONVALUE
					.draft(pos);
			Stream.of(inTop, inTopValue, position, positionValue)
					.map(MessageDraft::prefixed)
					.collect(MessageDraft.join(MessageDraft.newline()))
					.send(sender);
		} catch (Exception e) {
			MainLangKey.ERROR_COMMAND_EXECUTION.draft()
					.sendPrefixed(sender);
			getPlugin().logger().severe(e, "Ann error occurred while executing top info command");
		}
	}

}