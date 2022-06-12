package me.szumielxd.portfel.common.luckperms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.luckperms.context.PortfelBalanceCalculator;
import me.szumielxd.portfel.common.luckperms.context.PortfelTopEnabledCalculator;
import me.szumielxd.portfel.common.luckperms.context.PortfelTopPosCalculator;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextManager;

public class ContextProvider<T> {
	
	private final @NotNull Portfel plugin;
	private final @NotNull Class<? extends T> playerClass;
	private final @Nullable ContextManager contextManager;
	private final @NotNull List<ContextCalculator<?>> registeredCalculators = new ArrayList<>();
	
	
	public ContextProvider(@NotNull Portfel plugin, @NotNull Class<? extends T> playerClass) {
		this.plugin = plugin;
		this.playerClass = playerClass;
		ContextManager mgr = null;
		try {
			Class.forName("net.luckperms.api.LuckPermsProvider");
			LuckPerms luckPerms = LuckPermsProvider.get();
			if (luckPerms == null) {
				throw new IllegalStateException("LuckPerms API not loaded.");
			}
			mgr = luckPerms.getContextManager();
			this.setup();
		} catch (ClassNotFoundException e) {
			// luckperms not loaded
		}
		this.contextManager = mgr;
	}
	
	
	private void setup() {
		this.register(() -> new PortfelBalanceCalculator<>(this.plugin, "portfel:balance", this.playerClass));
		this.register(() -> new PortfelTopPosCalculator<>(this.plugin, "portfel:top-position", this.playerClass));
		this.register(() -> new PortfelTopEnabledCalculator<>(this.plugin, "portfel:top-enabled", this.playerClass));
	}
	
	
	public void unregisterAll() {
		this.registeredCalculators.forEach(this.contextManager::unregisterCalculator);
		this.registeredCalculators.clear();
	}
	
	
	private void register(Supplier<ContextCalculator<T>> calculatorSupplier) {
		ContextCalculator<T> calculator = calculatorSupplier.get();
		this.contextManager.registerCalculator(calculator);
		this.registeredCalculators.add(calculator);
	}
	

}
