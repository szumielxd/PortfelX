package me.szumielxd.portfel.bukkit.api.objects;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DoneCondition extends Predicate<Player> {
	
	
	@Override
	public boolean test(@NotNull Player player);
	
	
	
	public static enum OperationType {
		
		
		EQUAL("==", (s1, s2) -> {
			if (s1.equalsIgnoreCase(s2)) return true;
			try {
				if (Double.parseDouble(s1) == Double.parseDouble(s2)) return true;
			} catch (NumberFormatException e) {}
			return false;
		}),
		STRICT_EQUAL("===", (s1, s2) -> {
			if (s1.equals(s2)) return true;
			return false;
		}),
		BIGGER(">", (s1, s2) -> {
			try {
				if (Double.parseDouble(s1) > Double.parseDouble(s2)) return true;
			} catch (NumberFormatException e) {}
			return false;
		}),
		BIGGER_EQUAL(">=", (s1, s2) -> {
			try {
				if (Double.parseDouble(s1) >= Double.parseDouble(s2)) return true;
			} catch (NumberFormatException e) {}
			return false;
		}),
		SMALLER("<", (s1, s2) -> {
			try {
				if (Double.parseDouble(s1) < Double.parseDouble(s2)) return true;
			} catch (NumberFormatException e) {}
			return false;
		}),
		SMALLER_EQUAL("<=", (s1, s2) -> {
			try {
				if (Double.parseDouble(s1) <= Double.parseDouble(s2)) return true;
			} catch (NumberFormatException e) {}
			return false;
		}),
		;
		
		
		private final @NotNull Pattern pattern;
		private final @NotNull String sign;
		private final @NotNull BiPredicate<String, String> predicate;
		
		private OperationType(@NotNull String sign, @NotNull BiPredicate<String, String> predicate) {
			this.sign = Objects.requireNonNull(sign, "sign cannot be null");
			this.predicate = Objects.requireNonNull(predicate, "predicate cannot be null");
			this.pattern = Pattern.compile(String.format("(.*?) (|\\\\\\\\)%s (.*)", Pattern.quote(this.sign)));
		}
		
		public @NotNull String getSign() {
			return this.sign;
		}
		
		public @NotNull Pattern getPattern() {
			return this.pattern;
		}
		
		public boolean accept(@NotNull String left, @NotNull String right) {
			return this.predicate.test(Objects.requireNonNull(left, "left cannot be null"), Objects.requireNonNull(right, "right cannot be null"));
		}
		
		
	}
	

}
