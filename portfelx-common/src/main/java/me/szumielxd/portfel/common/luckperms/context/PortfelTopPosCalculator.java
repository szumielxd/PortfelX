package me.szumielxd.portfel.common.luckperms.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.managers.TopManager.TopEntry;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

public class PortfelTopPosCalculator<T> implements ContextCalculator<T> {
	
	
	private final Portfel plugin;
	private final String key;
	private final Method playerGetUniqueId;
	
	
	public PortfelTopPosCalculator(@NotNull Portfel plugin, @NotNull String key, Class<? extends T> playerClass) {
		this.plugin = plugin;
		this.key = key;
		try {
			this.playerGetUniqueId = playerClass.getMethod("getUniqueId");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public void calculate(@NotNull T target, @NotNull ContextConsumer consumer) {
		try {
			UUID uuid = (UUID) this.playerGetUniqueId.invoke(target);
			int pos = -1;
			List<TopEntry> top = this.plugin.getTopManager().getFullTopCopy();
			if (top != null) {
				for (int i = 0; i < top.size(); i++) {
					if (top.get(i).getUniqueId().equals(uuid)) {
						pos = i+1;
						break;
					}
				}
			}
			consumer.accept(this.key, String.valueOf(pos));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
    public ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(this.key, "-1")
                .add(this.key, "1")
                .add(this.key, "2")
                .add(this.key, "3")
                .add(this.key, "10")
                .build();
    }
	

}
