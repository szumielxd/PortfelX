package me.szumielxd.portfel.common.commands;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.common.lang.Lang.LangKey;
import me.szumielxd.portfel.common.lang.MainLangKey;
import me.szumielxd.portfel.common.utils.CollectionUtils;

public interface AbstractCommand<C> {
	
	
	public void onCommand(@NotNull CommonSender<C> sender, @NotNull ParsedCommandContext parsedContext);
	
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
	
	public default boolean validateCanUse(@NotNull CommonSender<C> sender) {
		if (!hasPermission(sender)) {
			MainLangKey.ERROR_COMMAND_PERMISSION.draft().sendPrefixed(sender);
			return false;
		}
		var access = getAccess();
		if (!access.canAccess(sender)) {
			access.getAccessMessage().draft().sendPrefixed(sender);
			return false;
		}
		return true;
	}
	
	
	@RequiredArgsConstructor
	public enum CommandAccess {
		
		ALL(s -> true, MainLangKey.EMPTY),
		PLAYERS(CommonPlayer.class::isInstance, MainLangKey.ERROR_COMMAND_PLAYERS_ONLY),
		CONSOLE(s -> !(s instanceof CommonPlayer), MainLangKey.ERROR_COMMAND_CONSOLE_ONLY),
		;
		
		private final @NotNull Predicate<CommonSender<?>>  validator;
		private final @NotNull LangKey key;
		
		public boolean canAccess(@Nullable CommonSender<?> sender) {
			return this.validator.test(sender);
		}
		
		public @NotNull LangKey getAccessMessage() {
			return this.key;
		}
		
	}
	
	public record ParsedArguments(@NotNull Object[] staticArgs, @NotNull Object[] flyingArgs) {
		
		public @NotNull ParsedArguments append(@NotNull ParsedArguments args) {
			return new ParsedArguments(
					CollectionUtils.mergeArrays(staticArgs, args.staticArgs),
					CollectionUtils.mergeArrays(flyingArgs, args.flyingArgs));
		}
		
	}
	
	public record ParsedCommandContext(@NotNull ParsedArguments parsedArgs, @NotNull String[] label, @NotNull String[] argsLeft) {
		
		public @NotNull ParsedCommandContext chain(@NotNull ParsedCommandContext context) {
			return new ParsedCommandContext(
					parsedArgs.append(context.parsedArgs),
					CollectionUtils.mergeArrays(label, context.label),
					context.argsLeft);
		}
		
		public @NotNull ParsedCommandContext skipArgsLeft(int count) {
			return new ParsedCommandContext(
					parsedArgs,
					CollectionUtils.mergeArrays(label, Stream.of(argsLeft).limit(count).toArray(String[]::new)),
					CollectionUtils.popArray(argsLeft, count));
		}
		
		public @Nullable String argLeft(int index) {
			if (argsLeft.length > index) {
				return argsLeft[index];
			}
			return null;
		}
		
		public @Nullable Object parsedStaticArg(int index) {
			if (parsedArgs.staticArgs.length > index) {
				return parsedArgs.staticArgs[index];
			}
			return null;
		}
		
		public @Nullable Object parsedFlyingArg(int index) {
			if (parsedArgs.flyingArgs.length > index) {
				return parsedArgs.flyingArgs[index];
			}
			return null;
		}
		
		public static @NotNull ParsedCommandContext initial(@NotNull String label, @NotNull String[] args) {
			return new ParsedCommandContext(
					new ParsedArguments(new Object[0], new Object[0]),
					new String[] { label },
					args);
		}
		
	}
	

}
