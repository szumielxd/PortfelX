package me.szumielxd.portfel.bungee.objects;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeePlayer extends BungeeSender implements ProxyPlayer {
	
	
private final @NotNull ProxiedPlayer player;
	
	
	public BungeePlayer(@NotNull PortfelBungeeImpl plugin, @NotNull ProxiedPlayer player) {
		super(plugin, player);
		this.player = player;
	}
	
	
	@Override
	public String getName() {
		return this.player.getName();
	}
	
	
	@Override
	public Collection<String> getGroups() {
		final Set<String> groups = new HashSet<>(this.player.getGroups());
		try {
			Class.forName("net.luckperms.api.LuckPermsProvider");
			LuckPerms api = LuckPermsProvider.get();
			User user = api.getUserManager().getUser(this.player.getUniqueId());
			ContextManager cm = api.getContextManager();
			QueryOptions queryOptions = cm.getQueryOptions(user).orElse(cm.getStaticQueryOptions());
			user.getNodes(NodeType.INHERITANCE).stream().map(NodeType.INHERITANCE::cast).filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context())).map(InheritanceNode::getGroupName).map(this::convertGroupDisplayName).forEachOrdered(groups::add);
		} catch(Exception e) {}
		return Collections.unmodifiableCollection(groups);
	}
	
	
	@Override
	public int getVersion() {
		return this.player.getPendingConnection().getVersion();
	}
	
	
	@Override
	public boolean isModded() {
		return this.player.isForgeUser();
	}
	
	
	@Override
	public void chat(@NotNull String message) {
		this.player.chat(message);
	}
	
	
	private String convertGroupDisplayName(String groupName) {
		Group group = LuckPermsProvider.get().getGroupManager().getGroup(groupName);
		if (group != null) {
			groupName = group.getFriendlyName();
		}
		return groupName;
	}


	@Override
	public @NotNull UUID getUniqueId() {
		return this.player.getUniqueId();
	}


	@Override
	public void disconnect(@NotNull String reason) {
		this.player.disconnect(TextComponent.fromLegacyText(reason));		
	}


	@Override
	public void connect(@NotNull String server) {
		this.player.connect(this.plugin.asPlugin().getProxy().getServerInfo(server));
	}


	@Override
	public void executeServerCommand(@NotNull String command) {
		this.player.chat("/" + command);
	}


	@Override
	public @NotNull String getWorldName() {
		return Optional.ofNullable(this.player.getServer()).map(s -> s.getInfo().getName()).orElse("");
	}


	@Override
	public @Nullable Locale locale() {
		return this.player.getLocale();
	}


	@Override
	public void sendToWorld(@NotNull String worldName) {
		this.connect(worldName);
	}


	@Override
	public void sendActionBar(@NotNull Component message) {
		this.plugin.adventure().player(this.player).sendActionBar(message);
	}


	@Override
	public void showTitle(@NotNull Component title, @NotNull Component subtitle, @Nullable Times times) {
		this.plugin.adventure().player(this.player).showTitle(Title.title(title, subtitle, times));
	}


	@Override
	public void showBossBar(@NotNull Component name, @NotNull Duration time, float progress, @NotNull Color color, @NotNull Overlay overlay, @NotNull Flag... flags) {
		final BossBar bar = BossBar.bossBar(name, progress, color, overlay, new HashSet<>(Arrays.asList(flags)));
		this.plugin.adventure().player(this.player).showBossBar(bar);
		this.plugin.getCommonServer().getScheduler().runTaskLater(() -> this.plugin.adventure().player(this.player).hideBossBar(bar), time.toMillis(), TimeUnit.MILLISECONDS);
	}


	@Override
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isConnected() {
		return this.player.isConnected();
	}


	@Override
	public Optional<ProxyServerConnection> getServer() {
		return Optional.ofNullable(this.player.getServer()).map(srv -> new BungeeServerConnection(this.plugin, srv));
	}
	

}
