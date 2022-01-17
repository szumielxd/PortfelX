package me.szumielxd.portfel.bungee.commands;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.bungee.objects.BungeeSender;
import me.szumielxd.portfel.proxy.commands.CommonCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommandWrapper extends Command implements TabExecutor {
	
	
	private final @NotNull PortfelBungeeImpl plugin;
	private final @NotNull CommonCommand command;
	

	public BungeeCommandWrapper(@NotNull PortfelBungeeImpl plugin, @NotNull CommonCommand command) {
		super(command.getName(), command.getPermission(), command.getAliases());
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.command = Objects.requireNonNull(command, "command cannot be null");
	}


	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return this.command.onTabComplete(BungeeSender.wrap(this.plugin, sender), args);
	}


	@Override
	public void execute(CommandSender sender, String[] args) {
		this.command.execute(BungeeSender.wrap(this.plugin, sender), args);
	}

}
