package me.szumielxd.portfel.velocity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import lombok.RequiredArgsConstructor;
import me.szumielxd.portfel.api.CommonLogger;

@RequiredArgsConstructor
public class VelocityLogger implements CommonLogger {
	
	
	private final Logger logger;
	
	
	@Override
	public void info(@NotNull String message) {
		logger.info(message);
	}

	@Override
	public void info(@NotNull String format, @Nullable Object... args) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format(format, args));
		}
	}

	@Override
	public void warn(@NotNull String message) {
		logger.warn(message);
	}

	@Override
	public void warn(@NotNull String format, @Nullable Object... args) {
		if (logger.isWarnEnabled()) {
			logger.warn(String.format(format, args));
		}
	}

	@Override
	public void warn(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args) {
		if (logger.isWarnEnabled()) {
			logger.warn(String.format(format, args), throwable);
		}
	}

	@Override
	public void severe(@NotNull String message) {
		logger.error(message);
	}

	@Override
	public void severe(@NotNull String format, @Nullable Object... args) {
		if (logger.isErrorEnabled()) {
			logger.error(String.format(format, args));
		}
	}

	@Override
	public void severe(@NotNull Throwable throwable, @NotNull String format, @Nullable Object... args) {
		if (logger.isErrorEnabled()) {
			logger.error(String.format(format, args), throwable);
		}
	}
	

}
