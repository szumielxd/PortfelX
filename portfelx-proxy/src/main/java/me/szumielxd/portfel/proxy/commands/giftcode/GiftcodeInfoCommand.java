package me.szumielxd.portfel.proxy.commands.giftcode;

import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.ActionExecutor;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.commands.AbstractCommand;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.utils.MiscUtils;
import me.szumielxd.portfel.proxy.objects.PrizeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class GiftcodeInfoCommand extends SimpleCommand {

	public GiftcodeInfoCommand(@NotNull Portfel plugin, @NotNull AbstractCommand parent) {
		super(plugin, parent, "info", "information", "informations", "get", "about");
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		PrizeToken token = (PrizeToken) parsedArgs[0];
		Component header = Portfel.PREFIX.append(Component.text("> ", LIGHT_PURPLE, BOLD)).append(LangKey.COMMAND_GIFTCODE_INFO_HEADER.component(DARK_PURPLE,
				this.prepareInteractive(Component.text(token.getToken(), WHITE), label, token.getToken())));
		
		String[] labelUser = new String[] { label[0], "user" };
		UUID creatorId = token.getCreator().getUniqueId();
		boolean creatorIsPlayer = !(creatorId.equals(ActionExecutor.CONSOLE_UUID) || creatorId.equals(ActionExecutor.PLUGIN_UUID));
		String id = !creatorIsPlayer? "null" : creatorId.toString();
		
		Component creator = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_CREATOR.component(LIGHT_PURPLE,
				this.prepareInteractive(Component.text(token.getCreator().getDisplayName(), WHITE), labelUser, token.getCreator().getDisplayName())));
		Component uuid = Portfel.PREFIX.append(Component.text("   ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_UUID.component(DARK_PURPLE,
				this.prepareInteractive(Component.text(id, WHITE), labelUser, id)));
		
		Component order = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_ORDER.component(DARK_PURPLE, Component.text(token.getOrder(), AQUA)));
		
		Component accessibility = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_ACCESSIBILITY.component(LIGHT_PURPLE));
		Component accessType = Portfel.PREFIX.append(Component.text("   ")).append(LangKey.COMMAND_GIFTCODE_INFO_ACCESSTYPE.component(DARK_PURPLE,
				Component.text(MiscUtils.firstToUpper(token.getSelectorType().name(), true), AQUA)));
		Component accessList = Portfel.PREFIX.append(Component.text("   ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_ACCESSLIST.component(DARK_PURPLE,
				MiscUtils.join(Component.text(", ",  GRAY), token.getServerNames().stream().map(s -> Component.text(s, AQUA)).collect(Collectors.toList()))));
		
		Component dates = Portfel.PREFIX.append(Component.text("- ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_DATES.component(LIGHT_PURPLE));
		Component creation = Portfel.PREFIX.append(Component.text("   ")).append(LangKey.COMMAND_GIFTCODE_INFO_CREATION.component(DARK_PURPLE,
				Component.text(dateFormat.format(token.getCreationDate()), AQUA)));
		Component expiration = Portfel.PREFIX.append(Component.text("   ", WHITE)).append(LangKey.COMMAND_GIFTCODE_INFO_EXPIRATION.component(DARK_PURPLE,
				token.getExpiration() == -1 ? LangKey.COMMAND_LISTGIFTCODES_LIFETIME.component(AQUA) : Component.text(dateFormat.format(new Date(token.getExpiration())), AQUA)));
		sender.sendTranslated(MiscUtils.join(Component.newline(), header, creator, uuid, order, accessibility, accessType, accessList, dates, creation, expiration));
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_USER_INFO_DESCRIPTION;
	}
	
	private @NotNull Component prepareInteractive(@NotNull Component comp, @NotNull String[] label, @NotNull String text) {
		return comp.hoverEvent(Component.text(text, AQUA)
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_SUGGEST.component(GRAY))
				.append(Component.newline()).append(Component.text("» ", DARK_GRAY)).append(LangKey.COMMAND_USER_INFO_INSERT.component(GRAY)))
				.clickEvent(ClickEvent.suggestCommand("/" + String.join(" ", Arrays.copyOf(label, 2)) + " " + text)).insertion(text);
	}

}