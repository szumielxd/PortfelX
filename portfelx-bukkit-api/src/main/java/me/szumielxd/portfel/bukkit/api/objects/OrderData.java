package me.szumielxd.portfel.bukkit.api.objects;

import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
@AllArgsConstructor
public class OrderData {
	
	
	protected final @NotNull String orderName;
	protected final int slot;
	protected final int level;
	protected final OrderDisplay display;
	protected final long price;
	protected final @NotNull OrderConditions conditions;
	protected final OrderActions actions;
	
	public @NotNull OrderDataOnAir onAirWithPrice(long price) {
		return new OrderDataOnAir(this.orderName, this.display.displayName(), price, this.actions);
	}
	
	@Getter
	@AllArgsConstructor
	public static final class OrderDataOnAir {
		
		
		protected final String orderName;
		protected final Component displayName;
		protected final long price;
		protected final OrderActions actions;
		
	}
	
	public record OrderDisplay(@NotNull Component displayName, @NotNull List<Component> description, @NotNull List<Component> denyDescription, @NotNull OrderIcons icons) {}
	
	public record OrderIcons(@NotNull ItemStack icon, @NotNull ItemStack iconBought, @NotNull ItemStack iconDenied) {}
	
	public record OrderActions(@NotNull List<String> broadcasts, @NotNull List<String> messages, @NotNull List<String> commands) {}
	
	public record OrderConditions(@NotNull Optional<String> donePermission, @NotNull List<DoneCondition> doneConditions, @NotNull List<DoneCondition> denyConditions) {
		
		private boolean isDenied(@NotNull Player player) {
			return !denyConditions.isEmpty()
					&& denyConditions.stream().allMatch(c -> c.test(player));
		}
		
		private boolean isDone(@NotNull Player player) {
			/*
			 * null && true -> true
			 * true && null -> true
			 * true && true -> true
			 * null && null -> false
			 * null && false -> false
			 * true && false -> false
			 * false && null -> false
			 * false && true -> false
			 * false && false -> false
			 * 
			 * isEmpty1 != isEmpty1
			 * true1|null1 == true2|null2
			 */
			return doneConditions.isEmpty() == donePermission.isEmpty()
					&& doneConditions.stream().allMatch(c -> c.test(player)) == donePermission.map(player::hasPermission).orElse(true);
		}
		
		public Availability checkAvailability(@NotNull Player player) {
			if (isDenied(player)) {
				return Availability.DENIED;
			}
			if (isDone(player)) {
				return Availability.DONE;
			}
			return Availability.AVAILABLE;
		}
		
	}
	
	public enum Availability {
		AVAILABLE,
		DONE,
		DENIED
	}
	

}
