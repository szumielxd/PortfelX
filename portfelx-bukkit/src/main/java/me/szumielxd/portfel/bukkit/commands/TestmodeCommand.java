package me.szumielxd.portfel.bukkit.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import net.kyori.adventure.text.Component;

public class TestmodeCommand extends SimpleCommand<Component> {
	
	
	@Getter private final @NotNull List<CmdArg> staticArgs = List.of();
	@Getter private final @NotNull List<CmdArg> flyingArgs = List.of();
	@Getter private final @NotNull LangKey description = BukkitLangKey.COMMAND_TESTMODE_DESCRIPTION;
	@Getter private final @NotNull CommandAccess access = CommandAccess.PLAYERS;
	

	public TestmodeCommand(@NotNull PortfelBukkitImpl plugin, @NotNull MainCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender<Component> sender, @NotNull ParsedCommandContext parsedContext) {
		
		try {
			User user = this.getPlugin().getUserManager().getOrCreateUser(((CommonPlayer<Component>) sender).getUniqueId());
			if (user instanceof BukkitOperableUser operableUser) {
				BukkitLangKey.COMMAND_TESTMODE_EXECUTE
						.draft(operableUser.toggleTestMode() ? MainLangKey.MAIN_VALUE_ON : MainLangKey.MAIN_VALUE_OFF)
						.send(sender, true);
				return;
			}
		} catch (Exception e) {
			// empty catch
		}
		MainLangKey.ERROR_COMMAND_USER_NOT_LOADED
				.draft()
				.send(sender, true);
	}

	
	
	
	
}
