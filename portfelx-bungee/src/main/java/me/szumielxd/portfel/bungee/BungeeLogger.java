package me.szumielxd.portfel.bungee;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.CommonLogger;

@RequiredArgsConstructor
public class BungeeLogger implements CommonLogger {
	
	
	private final Logger logger;
	

	@Override
	public void info(@NotNull String message) {
		logger.info(message);
	}

	@Override
	public void info(@NotNull String format, @Nullable Object... args) {
		logger.info(() -> String.format(format, args));
	}

	@Override
	public void warn(@NotNull String message) {
		logger.warning(message);
	}

	@Override
	public void warn(@NotNull String format, @Nullable Object... args) {
		logger.warning(() -> String.format(format, args));
	}

	@Override
	public void warn(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args) {
		logger.log(Level.WARNING, throwable, () -> String.format(format, args));
	}

	@Override
	public void severe(@NotNull String message) {
		logger.severe(message);
	}

	@Override
	public void severe(@NotNull String format, @Nullable Object... args) {
		logger.severe(() -> String.format(format, args));
	}

	@Override
	public void severe(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args) {
		logger.log(Level.SEVERE, throwable, () -> String.format(format, args));
	}

}
