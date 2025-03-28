package me.szumielxd.portfel.bukkit.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.PortfelBukkitImpl;
import me.szumielxd.portfel.bukkit.gui.MainPortfelGui;
import me.szumielxd.portfel.bukkit.gui.PortfelGuiHolder;
import me.szumielxd.portfel.bukkit.managers.BukkitUserManagerImpl;
import me.szumielxd.portfel.bukkit.objects.BukkitOperableUser;

class CommandsTest {
	
	private ServerMock server;
	private PortfelBukkitImpl plugin;
	private PlayerMock player;
	
	@BeforeEach
	void setup() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(PortfelBukkitImpl.class);
		player = new PlayerMock(server, "test_dummy") {
			@Override
			public int getProtocolVersion() {
				return 735; // 1.16.0
			}
		};
	}
	
	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}
	
	@Test
	void checkIfLoaded() {
		assertTrue(plugin.isEnabled());
	}
	
	@Test
	void helpCommand() throws InterruptedException {
		server.addPlayer(player);
		player.setOp(true);
		assertTrue(player.performCommand("dp"));
		Thread.sleep(50); // wait for async command completion
		player.nextMessage(); // insignificant header
		assertEquals("§b§l[§5§lP§b§l]§3 Use §b/dp help§3 to view available commands.", player.nextMessage());
	}
	
	@Test
	void guiCommand() {
		server.addPlayer(player);
		player.setOp(true);
		injectUser(plugin, player);
		assertTrue(player.performCommand("wallet"));
		var holder = player.getOpenInventory().getTopInventory().getHolder();
		assertInstanceOf(PortfelGuiHolder.class, holder);
		var gui = ((PortfelGuiHolder) holder).getGui();
		assertInstanceOf(MainPortfelGui.class, gui);
	}
	
	
	@SuppressWarnings("unchecked")
	private static void injectUser(@NotNull PortfelBukkitImpl portfel, @NotNull PlayerMock pl, long balance, long minorBalance) {
		try {
			var field = BukkitUserManagerImpl.class.getDeclaredField("users");
			field.setAccessible(true);
			var map = (Map<UUID, User>) field.get(portfel.getUserManager());
			map.put(pl.getUniqueId(), new BukkitOperableUser(portfel, pl.getUniqueId(), pl.getName(), false, balance,  minorBalance, UUID.randomUUID()));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void injectUser(@NotNull PortfelBukkitImpl portfel, @NotNull PlayerMock pl, long balance) {
		injectUser(portfel, pl, balance, 1000);
	}
	
	private static void injectUser(@NotNull PortfelBukkitImpl portfel, @NotNull PlayerMock pl) {
		injectUser(portfel, pl, 420);
	}
	

}
