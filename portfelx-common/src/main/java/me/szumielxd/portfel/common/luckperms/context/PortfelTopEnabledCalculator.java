package me.szumielxd.portfel.common.luckperms.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.User;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

public class PortfelTopEnabledCalculator<T> implements ContextCalculator<T> {
	
	
	private final Portfel plugin;
	private final String key;
	private final Method playerGetUniqueId;
	
	
	public PortfelTopEnabledCalculator(@NotNull Portfel plugin, @NotNull String key, Class<? extends T> playerClass) {
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
			User user = this.plugin.getUserManager().getUser(uuid);
			consumer.accept(this.key, user == null ? "true" : String.valueOf(!user.isDeniedInTop()));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
    public ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(this.key, "false")
                .add(this.key, "true")
                .build();
    }
	

}
