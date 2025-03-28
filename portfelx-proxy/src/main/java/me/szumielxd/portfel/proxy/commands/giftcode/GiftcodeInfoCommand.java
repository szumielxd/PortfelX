package me.szumielxd.portfel.proxy.commands.giftcode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.commands.common.ParentLikeCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.draft.MessageDraft;
import me.szumielxd.portfel.common.utils.FormatUtils;
import me.szumielxd.portfel.proxy.lang.ProxyLangKey;
import me.szumielxd.portfel.proxy.objects.PrizeToken;

public class GiftcodeInfoCommand<C> extends SimpleCommand<C> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = ProxyLangKey.COMMAND_GIFTCODE_INFO_DESCRIPTION;
	

	public GiftcodeInfoCommand(@NotNull Portfel<C> plugin, @NotNull ParentLikeCommand<C> parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		PrizeToken token = (PrizeToken) parsedContext.parsedStaticArg(0);
		String[] label = parsedContext.label();
		String commandPrefix = String.join(" ", Arrays.copyOf(label, 2)) + " ";
		String commandUserPrefix = label[0] + " user";
		UUID creatorId = token.getCreator().getUniqueId();
		boolean creatorIsNotPlayer = creatorId.equals(ActionExecutor.CONSOLE_UUID) || creatorId.equals(ActionExecutor.PLUGIN_UUID);
		String id = creatorIsNotPlayer ? "null" : creatorId.toString();
		
		var header = ProxyLangKey.COMMAND_USER_INFO_HEADER
				.draft(interactiveValue(token.getToken(), commandPrefix));
		var creator = ProxyLangKey.COMMAND_GIFTCODE_INFO_CREATOR
				.draft(interactiveValue(token.getCreator().getDisplayName(), commandUserPrefix));
		var uuid = ProxyLangKey.COMMAND_GIFTCODE_INFO_UUID
				.draft(interactiveValue(id, commandUserPrefix));
		var order = ProxyLangKey.COMMAND_GIFTCODE_INFO_ORDER
				.draft(token.getOrder());
		var accessibility = ProxyLangKey.COMMAND_GIFTCODE_INFO_ACCESSIBILITY
				.draft();
		var accessType = ProxyLangKey.COMMAND_GIFTCODE_INFO_ACCESSTYPE
				.draft(FormatUtils.firstToUpper(token.getSelectorType().name(), true));
		var accessList = ProxyLangKey.COMMAND_GIFTCODE_INFO_ACCESSLIST
				.draft(token.getServerNames().stream()
						.map(ProxyLangKey.COMMAND_GIFTCODE_INFO_ACCESSLIST_FORMAT::draft)
						.collect(MessageDraft.join(", ")));
		var dates = ProxyLangKey.COMMAND_GIFTCODE_INFO_DATES
				.draft();
		var creation = ProxyLangKey.COMMAND_GIFTCODE_INFO_CREATION
				.draft(dateFormat.format(token.getCreationDate()));
		var expiration = ProxyLangKey.COMMAND_GIFTCODE_INFO_EXPIRATION
				.draft(token.getExpiration() != -1 ?
						dateFormat.format(new Date(token.getExpiration()))
						: ProxyLangKey.COMMAND_LISTGIFTCODES_LIFETIME);
		
		Stream.of(header, creator, uuid, order, accessibility, accessType, accessList, dates, creation, expiration)
				.map(MessageDraft::prefixed)
				.collect(MessageDraft.join(MessageDraft.newline()))
				.send(sender);
	}
	
	private @NotNull MessageDraft interactiveValue(@NotNull String value, @NotNull String commandPrefix) {
		return ProxyLangKey.COMMAND_GIFTCODE_INFO_VALUE
				.draft(value, commandPrefix, ProxyLangKey.COMMAND_GIFTCODE_INFO_VALUEHOVER);
	}

}