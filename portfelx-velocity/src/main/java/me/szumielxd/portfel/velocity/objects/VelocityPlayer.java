package me.szumielxd.portfel.velocity.objects;

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

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;

import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import me.szumielxd.portfel.velocity.PortfelVelocityImpl;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

public class VelocityPlayer extends VelocitySender implements ProxyPlayer {

	
	private final @NotNull Player player;
	
	
	public VelocityPlayer(@NotNull PortfelVelocityImpl plugin, @NotNull Player player) {
		super(plugin, player);
		this.player = player;
	}
	
	
	@Override
	public String getName() {
		return this.player.getUsername();
	}
	
	
	@Override
	public Collection<String> getGroups() {
		final Set<String> groups = new HashSet<>();
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
		return this.player.getProtocolVersion().getProtocol();
	}
	
	
	@Override
	public boolean isModded() {
		return this.player.getModInfo().isPresent();
	}
	
	
	@Override
	public void chat(@NotNull String message) {
		this.player.spoofChatInput(message);
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
		this.player.disconnect(LegacyComponentSerializer.legacySection().deserialize(reason));		
	}


	@Override
	public void connect(@NotNull String server) {
		this.plugin.getProxy().getServer(server).map(player::createConnectionRequest).ifPresent(ConnectionRequestBuilder::fireAndForget);
	}


	@Override
	public void executeServerCommand(@NotNull String command) {
		this.player.spoofChatInput("/" + command);
	}


	@Override
	public @NotNull String getWorldName() {
		return this.player.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("");
	}


	@Override
	public @Nullable Locale locale() {
		return this.player.getEffectiveLocale();
	}


	@Override
	public void sendToWorld(@NotNull String worldName) {
		this.connect(worldName);
	}


	@Override
	public void sendActionBar(@NotNull Component message) {
		this.player.sendActionBar(message);
	}


	@Override
	public void showTitle(@NotNull Component title, @NotNull Component subtitle, @Nullable Times times) {
		this.player.showTitle(Title.title(title, subtitle, times));
	}


	@Override
	public void showBossBar(@NotNull Component name, @NotNull Duration time, float progress, @NotNull Color color, @NotNull Overlay overlay, @NotNull Flag... flags) {
		final BossBar bar = BossBar.bossBar(name, progress, color, overlay, new HashSet<>(Arrays.asList(flags)));
		this.player.showBossBar(bar);
		this.plugin.getCommonServer().getScheduler().runTaskLater(() -> this.player.hideBossBar(bar), time.toMillis(), TimeUnit.MILLISECONDS);
	}


	@Override
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message) {
		this.player.sendPluginMessage(tag.indexOf(':') >= 0 ? MinecraftChannelIdentifier.from(tag) : new LegacyChannelIdentifier(tag), message);
	}


	@Override
	public boolean isConnected() {
		return this.player.isActive();
	}


	@Override
	public Optional<ProxyServerConnection> getServer() {
		return this.player.getCurrentServer().map(srv -> new VelocityServerConnection(this.plugin, srv));
	}

}
