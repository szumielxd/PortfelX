package me.szumielxd.portfel.bukkit.objects;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.bukkit.api.objects.DoneCondition;

public class DoneConditionImpl implements DoneCondition {
	
	
	private final @NotNull String left;
	private final @NotNull String right;
	private final @NotNull OperationType operationType;
	
	
	public DoneConditionImpl(@NotNull String left, @NotNull String right, @NotNull OperationType operationType) {
		this.left = Objects.requireNonNull(left, "left cannot be null");
		this.right = Objects.requireNonNull(right, "right cannot be null");
		this.operationType = Objects.requireNonNull(operationType, "operationType cannot be null");
	}
	
	
	@Override
	public boolean test(@NotNull Player player) {
		String left = this.left;
		String right = this.right;
		try {
			Class.forName("be.maximvdw.placeholderapi.PlaceholderAPI");
			left = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, left);
			right = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, right);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			left = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, left);
			right = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, right);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return this.operationType.accept(left, right);
	}
	

}
