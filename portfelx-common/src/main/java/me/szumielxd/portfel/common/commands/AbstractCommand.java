package me.szumielxd.portfel.common.commands;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;

public interface AbstractCommand<C> {
	
	
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull Object[] parsedArgs, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull List<String> onTabComplete(@NotNull CommonSender<C> sender, @NotNull String[] label, @NotNull String[] args);
	
	public @NotNull String getName();
	
	public @NotNull String[] getAliases();
	
	public @NotNull List<CmdArg> getStaticArgs();
	
	public @NotNull List<CmdArg> getFlyingArgs();
	
	public @NotNull String getPermission();
	
	public @NotNull LangKey getDescription();
	
	public default @NotNull List<CmdArg> getAllArgs() {
		return Stream.concat(
				getStaticArgs().stream(),
				getFlyingArgs().stream()).toList();
	}
	
	public default @NotNull CommandAccess getAccess() {
		return CommandAccess.ALL;
	}
	
	public default boolean hasPermission(@NotNull CommonSender<C> sender) {
		return sender.hasPermission(this.getPermission());
	}
	
	public default boolean canUse(@NotNull CommonSender<C> sender) {
		return hasPermission(sender) && getAccess().canAccess(sender);
	}
	
	
	@RequiredArgsConstructor
	public enum CommandAccess {
		
		ALL(s -> true, MainLangKey.EMPTY),
		PLAYERS(CommonPlayer.class::isInstance, MainLangKey.ERROR_COMMAND_PLAYERS_ONLY),
		CONSOLE(s -> !(s instanceof CommonPlayer), MainLangKey.ERROR_COMMAND_CONSOLE_ONLY),
		;
		
		private final @NotNull Predicate<CommonSender<?>>  validator;
		private final @NotNull LangKey key;
		
		public boolean canAccess(CommonSender<?> sender) {
			return this.validator.test(sender);
		}
		
		public LangKey getAccessMessage() {
			return this.key;
		}
		
	}
	

}
