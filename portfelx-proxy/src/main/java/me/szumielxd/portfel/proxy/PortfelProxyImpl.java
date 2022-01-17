package me.szumielxd.portfel.proxy;

import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.api.managers.TaskManager;
import me.szumielxd.portfel.api.managers.UserManager;
import me.szumielxd.portfel.common.managers.PrizesManager;
import me.szumielxd.portfel.proxy.api.PortfelProxy;
import me.szumielxd.portfel.proxy.api.managers.ProxyTopManager;
import me.szumielxd.portfel.proxy.api.objects.CommonProxy;
import me.szumielxd.portfel.proxy.database.AbstractDB;
import me.szumielxd.portfel.proxy.database.AbstractDBLogger;
import me.szumielxd.portfel.proxy.database.token.AbstractTokenDB;
import me.szumielxd.portfel.proxy.managers.AccessManagerImpl;
import me.szumielxd.portfel.proxy.managers.OrdersManager;
import me.szumielxd.portfel.proxy.managers.TokenManager;

public interface PortfelProxyImpl extends PortfelProxy {
	
	
	public @NotNull Logger getLogger();
	
	public @NotNull CommonProxy getProxyServer();
	
	public @NotNull String getName();
	
	public @NotNull String getVersion();
	
	public @NotNull String getDescriptionText();
	
	public @NotNull String getAuthor();
	
	public @NotNull AbstractDB getDB();
	
	
	public @NotNull AbstractTokenDB getTokenDB();
	
	
	public @NotNull AccessManagerImpl getAccessManager();
	
	
	public @NotNull TokenManager getTokenManager();


	@Override
	public @NotNull UserManager getUserManager();
	
	
	@Override
	public @NotNull ProxyTopManager getTopManager();
	
	
	@Override
	public @NotNull TaskManager getTaskManager();
	
	public @NotNull OrdersManager getOrdersManager();
	
	public @NotNull PrizesManager getPrizesManager();
	
	/**
	 * Get database-oriented transaction logger.
	 * 
	 * @return transaction logger
	 */
	public @NotNull AbstractDBLogger getDBLogger();
	
	
	public void load();
	
	public void unload();
	
	public void onEnable();
	
	public void onDisable();
	

}
