package me.szumielxd.portfel.bukkit.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.gui.AbstractPortfelGui;
import me.szumielxd.portfel.bukkit.gui.MainPortfelGui;
import me.szumielxd.portfel.bukkit.gui.OrderPortfelGui;
import me.szumielxd.portfel.bukkit.gui.PortfelGuiHolder;
import me.szumielxd.portfel.bukkit.objects.BukkitSender;
import me.szumielxd.portfel.common.lang.MainLangKey;

public class WalletCommand implements TabExecutor {

	
	private final PortfelBukkitImpl plugin;
	
	
	public WalletCommand(@NotNull PortfelBukkitImpl plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1 && sender instanceof Player) {
			String arg = args[0].toLowerCase();
			return this.plugin.getOrdersManager().getNames().stream()
					.filter(s -> s.toLowerCase().startsWith(arg))
					.toList();
		}
		return List.of();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		var wrapper = BukkitSender.wrap(this.plugin, sender);
		if (!(sender instanceof Player player)) {
			MainLangKey.ERROR_COMMAND_PLAYERS_ONLY.draft().send(wrapper, true);
			return true;
		}
		AbstractPortfelGui gui = null;
		if (args.length > 0) {
			gui = this.plugin.getOrdersManager().getGui(args[0]);
		}
		if (gui == null) {
			gui = new MainPortfelGui(this.plugin, this.plugin.getOrdersManager().getOrderGuis().toArray(OrderPortfelGui[]::new));
		}
		User user = this.plugin.getUserManager().getUser(player.getUniqueId());
		if (user == null) {
			MainLangKey.ERROR_COMMAND_USER_NOT_LOADED.draft().send(wrapper, true);
			return true;
		}
		PortfelGuiHolder holder = new PortfelGuiHolder(this.plugin, gui, user, player);
		holder.getGui().setup(player, holder.getInventory());
		return true;
	}

}
