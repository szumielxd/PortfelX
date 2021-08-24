package me.szumielxd.portfel.common.luckperms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.common.luckperms.context.PortfelBalanceCalculator;
import me.szumielxd.portfel.common.luckperms.context.PortfelTopEnabledCalculator;
import me.szumielxd.portfel.common.luckperms.context.PortfelTopPosCalculator;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextManager;

public class ContextProvider {
	
	private final Portfel plugin;
	private ContextManager contextManager;
	private final List<ContextCalculator<?>> registeredCalculators = new ArrayList<>();
	
	
	public ContextProvider(@NotNull Portfel plugin) {
		this.plugin = plugin;
		try {
			Class.forName("net.luckperms.api.LuckPermsProvider");
			LuckPerms luckPerms = LuckPermsProvider.get();
			if (luckPerms == null) {
				throw new IllegalStateException("LuckPerms API not loaded.");
			}
			this.contextManager = luckPerms.getContextManager();
			this.setup();
		} catch (ClassNotFoundException e) {}
	}
	
	
	private void setup() {
		this.register("portfel:balance", () -> new PortfelBalanceCalculator<>(this.plugin, "portfel:balance"));
		this.register("portfel:top-position", () -> new PortfelTopPosCalculator<>(this.plugin, "portfel:top-position"));
		this.register("portfel:top-enabled", () -> new PortfelTopEnabledCalculator<>(this.plugin, "portfel:top-enabled"));
	}
	
	
	public void unregisterAll() {
		this.registeredCalculators.forEach(c -> this.contextManager.unregisterCalculator(c));
		this.registeredCalculators.clear();
	}
	
	
	private void register(String option, Supplier<ContextCalculator<?>> calculatorSupplier) {
		ContextCalculator<?> calculator = calculatorSupplier.get();
		this.contextManager.registerCalculator(calculator);
		this.registeredCalculators.add(calculator);
	}
	

}
