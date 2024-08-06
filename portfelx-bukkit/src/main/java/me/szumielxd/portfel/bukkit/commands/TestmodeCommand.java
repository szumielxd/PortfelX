package me.szumielxd.portfel.bukkit.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.lang.BukkitLangKey;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import net.kyori.adventure.text.Component;

public class TestmodeCommand extends SimpleCommand<Component> {
	

	public TestmodeCommand(@NotNull PortfelBukkitImpl plugin, @NotNull MainCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender<Component> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		
		try {
			User user = this.getPlugin().getUserManager().getOrCreateUser(((CommonPlayer<Component>) sender).getUniqueId());
			if (user instanceof BukkitOperableUser operableUser) {
				BukkitLangKey.COMMAND_TESTMODE_EXECUTE
						.draft(operableUser.toggleTestMode() ? BukkitLangKey.MAIN_VALUE_ON : BukkitLangKey.MAIN_VALUE_OFF)
						.send(sender, true);
				return;
			}
		} catch (Exception e) {
			// empty catch
		}
		BukkitLangKey.ERROR_COMMAND_USER_NOT_LOADED
				.draft()
				.send(sender, true);
	}
	
	@Override
	public @NotNull CommandAccess getAccess() {
		return CommandAccess.PLAYERS;
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
		return BukkitLangKey.COMMAND_TESTMODE_DESCRIPTION;
	}

	
	
	
	
}
