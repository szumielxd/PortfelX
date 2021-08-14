package me.szumielxd.portfel.bukkit.commands;

import static net.kyori.adventure.text.format.NamedTextColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.PortfelBukkit;
import me.szumielxd.portfel.bukkit.gui.AbstractPortfelGui;
import me.szumielxd.portfel.bukkit.gui.MainPortfelGui;
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui;
import me.szumielxd.portfel.bukkit.gui.PortfelGuiHolder;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.Portfel;
import me.szumielxd.portfel.common.objects.User;

public class WalletCommand implements TabExecutor {

	
	private final PortfelBukkit plugin;
	
	
	public WalletCommand(@NotNull PortfelBukkit plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1 && sender instanceof Player) {
			String arg = args[0].toLowerCase();
			return this.plugin.getOrdersManager().getNames().stream().filter(s -> s.toLowerCase().startsWith(arg)).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			BukkitSender.get(this.plugin, sender).sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_PLAYERS_ONLY.component(RED)));
			return true;
		}
		AbstractPortfelGui gui = null;
		if (args.length > 0) {
			gui = this.plugin.getOrdersManager().getGui(args[0]);
		}
		if (gui == null) gui = new MainPortfelGui(this.plugin, this.plugin.getOrdersManager().getOrderGuis().toArray(new OrderPortfelGui[0]));
		Player player = (Player) sender;
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user == null) {
			BukkitSender.get(this.plugin, sender).sendTranslated(Portfel.PREFIX.append(LangKey.ERROR_COMMAND_USER_NOT_LOADED.component(RED)));
			return true;
		}
		PortfelGuiHolder holder = new PortfelGuiHolder(this.plugin, gui, user);
		holder.getGui().setup(player, holder.getInventory());
		return true;
	}

}
