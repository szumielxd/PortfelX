package me.szumielxd.portfel.bukkit.commands;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.commands.CmdArg;
import me.szumielxd.portfel.common.commands.SimpleCommand;

public class TestmodeCommand extends SimpleCommand {
	

	public TestmodeCommand(@NotNull PortfelBukkitImpl plugin, @NotNull MainCommand parent, @NotNull String name, @NotNull String... aliases) {
		super(plugin, parent, name, aliases);
	}

	@Override
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args) {
		
		try {
			User user = this.getPlugin().getUserManager().getOrCreateUser(((CommonPlayer) sender).getUniqueId());
			if (user instanceof BukkitOperableUser) {
				LangKey result = ((BukkitOperableUser) user).toggleTestMode() ? LangKey.MAIN_VALUE_ON : LangKey.MAIN_VALUE_OFF;
				sender.sendTranslated(Portfel.PREFIX.append(LangKey.COMMAND_TESTMODE_EXECUTE.component(LIGHT_PURPLE, result.component(AQUA))));
				return;
			}
		} catch (Exception e) {
			// empty catch
		}
		sender.sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_USER_NOT_LOADED.component(RED)));
		
	}
	
	@Override
	public @NotNull CommandAccess getAccess() {
		return CommandAccess.PLAYERS;
	}

	@Override
	public @NotNull List<CmdArg> getArgs() {
		return this.emptyArgList;
	}

	@Override
	public @NotNull LangKey getDescription() {
		return LangKey.COMMAND_TESTMODE_DESCRIPTION;
	}

	
	
	
	
}
