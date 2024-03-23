package me.szumielxd.portfel.bungee.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.bungee.PortfelBungeeImpl;
import me.szumielxd.portfel.proxy.api.objects.ProxyPlayer;
import me.szumielxd.portfel.proxy.api.objects.ProxyServerConnection;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeePlayer extends BungeeSender implements ProxyPlayer<BaseComponent[]> {
	
	
private final @NotNull ProxiedPlayer player;
	
	
	public BungeePlayer(@NotNull PortfelBungeeImpl plugin, @NotNull ProxiedPlayer player) {
		super(plugin, player);
		this.player = player;
	}
	
	
	@Override
	public void sendMessage(@Nullable UUID source, @NotNull BaseComponent[] message) {
		this.player.sendMessage(source, message);
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
		} catch(Exception e) {
			// ignore
		}
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
	public @NotNull Locale locale() {
		return this.player.getLocale();
	}


	@Override
	public void sendToWorld(@NotNull String worldName) {
		this.connect(worldName);
	}


	@Override
	public void sendActionBar(@NotNull BaseComponent[] message) {
		this.player.sendMessage(ChatMessageType.ACTION_BAR, message);
	}


	@Override
	public void showTitle(@NotNull BaseComponent[] title, @NotNull BaseComponent[] subtitle, @Nullable TitleTiming times) {
		Title t = ProxyServer.getInstance().createTitle();
		t.title(title);
		t.subTitle(subtitle);
		if (times != null) {
			t.fadeIn((int) times.fadeIn().toMillis() / 50);
			t.stay((int) times.stay().toMillis() / 50);
			t.fadeOut((int) times.fadeOut().toMillis() / 50);
		}
		t.send(this.player);
	}


	@Override
	public void sendPluginMessage(@NotNull String tag, @NotNull byte[] message) {
		this.player.sendData(tag, message);
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
