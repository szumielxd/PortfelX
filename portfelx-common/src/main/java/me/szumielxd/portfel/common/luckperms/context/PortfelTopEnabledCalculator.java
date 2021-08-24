package me.szumielxd.portfel.common.luckperms.context;

import java.lang.reflect.InvocationTargetException;
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
	
	
	public PortfelTopEnabledCalculator(@NotNull Portfel plugin, @NotNull String key) {
		this.plugin = plugin;
		this.key = key;
	}
	

	@Override
	public void calculate(@NotNull T target, @NotNull ContextConsumer consumer) {
		try {
			UUID uuid = (UUID) target.getClass().getMethod("getUniqueId").invoke(target);
			User user = this.plugin.getUserManager().getUser(uuid);
			consumer.accept(this.key, user == null ? "true" : String.valueOf(!user.isDeniedInTop()));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
