package me.szumielxd.portfel.common.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.Lang.LangKey;
import me.szumielxd.portfel.common.objects.CmdArg;
import me.szumielxd.portfel.common.objects.CommonSender;

public interface AbstractCommand {
	
	
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull Iterable<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull String getName();
	
	public @NotNull String[] getAliases();
	
	public @NotNull List<CmdArg> getArgs();
	
	public @NotNull String getPermission();
	
	public @NotNull LangKey getDescription();
	
	default boolean hasPermission(@NotNull CommonSender sender) {
		return sender.hasPermission(this.getPermission());
	}
	

}
