package me.szumielxd.portfel.common.commands;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.Lang.LangKey;

public interface AbstractCommand {
	
	
	public void onCommand(@NotNull CommonSender sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull List<String> onTabComplete(@NotNull CommonSender sender, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull String getName();
	
	public @NotNull String[] getAliases();
	
	public @NotNull List<CmdArg> getArgs();
	
	public @NotNull String getPermission();
	
	public @NotNull LangKey getDescription();
	
	default public @NotNull CommandAccess getAccess() {
		return CommandAccess.ALL;
	}
	
	default public boolean hasPermission(@NotNull CommonSender sender) {
		return sender.hasPermission(this.getPermission());
	}
	
	
	public static enum CommandAccess {
		ALL(s -> true, LangKey.EMPTY),
		PLAYERS(s -> s instanceof CommonPlayer, LangKey.ERROR_COMMAND_PLAYERS_ONLY),
		CONSOLE(s -> !(s instanceof CommonPlayer), LangKey.ERROR_COMMAND_CONSOLE_ONLY),
		;
		
		private final Predicate<CommonSender>  validator;
		private final LangKey key;
		
		private CommandAccess(Predicate<CommonSender> validator, @NotNull LangKey key) {
			this.validator = validator;
			this.key = key;
		}
		
		public boolean canAccess(CommonSender sender) {
			return this.validator.test(sender);
		}
		
		public LangKey getAccessMessage() {
			return this.key;
		}
		
	}
	

}
