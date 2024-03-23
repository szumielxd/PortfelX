package me.szumielxd.portfel.common.managers;

import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.MemoryConfiguration;
import org.simpleyaml.configuration.file.YamlFile;

import com.google.gson.Gson;

import me.szumielxd.portfel.api.Portfel;
import me.szumielxd.portfel.api.objects.CommonPlayer;
import me.szumielxd.portfel.api.objects.CommonSender;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.common.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class PrizesManager {
	
	
	private final Portfel plugin;
	private final Path file;
	private Map<String, PrizeOrder> orders = new HashMap<>();
	
	
	public PrizesManager(Portfel plugin) {
		this.plugin = plugin;
		this.file = this.plugin.getDataFolder().resolve("token-prizes.yml");
	}
	
	
	public PrizesManager init() {
		YamlFile yaml = new YamlFile(this.file.toUri());
		// defaults
		MemoryConfiguration defaults = new MemoryConfiguration();
		ConfigurationSection defaultNothing = defaults.createSection("nothingOrder");
		defaultNothing.set("pattern", "nothing");
		defaultNothing.set("broadcast", List.of("&5Surprise! %player% won nothing!", "&bNow thanks God it's not about You!"));
		defaultNothing.set("message", List.of(GsonComponentSerializer.gson().serialize(Component.text("Congratultions %player%! You have won absolutly nothing!", LIGHT_PURPLE))));
		defaultNothing.set("command", List.of("kick %playerId% free kick!"));
		ConfigurationSection defaultVip = defaults.createSection("vip");
		defaultVip.set("pattern", "vip-([1-9]\\d*)d");
		defaultVip.set("broadcast", List.of("&5Surprise! %player% won VIP for $1 days!", "&bYou can do so by observing our fanpage: ig.example.com!"));
		defaultVip.set("command", List.of("lpb user %player% parent addtemp vip $1d"));
		yaml.addDefaults(defaults);
		try {
			if (yaml.exists()) {
				this.plugin.getLogger().info(String.format("Loading prizes from file `%s`", this.file.getFileName().toString()));
				yaml.load();
			} else {
				this.plugin.getLogger().info(String.format("Creating new prizes container as file `%s`", this.file.getFileName().toString()));
				yaml.setComment(defaultNothing.getCurrentPath(), 
						"""  
						This is the simplest example of creating new token-prize order.
						This prize order will be executed if pending order's name
						will match given pattern.
						""");
				yaml.setComment(defaultNothing.getCurrentPath() + ".pattern", 
						"""
						This is the only required value. Remember that this is REGEX pattern,
						so some characters are reserved for other purpose.
						""");
				yaml.setComment(defaultNothing.getCurrentPath() + ".pattern", 
						"""
						This is list of messages to broadcast when order will be executed.
						You can use (also in message and command) two placeholders: %player% for player's name
						and %playerId% for player's unique ID.
						""");
				yaml.setComment(defaultNothing.getCurrentPath() + ".pattern", 
						"""
						This is list of messages to send to player when order will be executed.
						You can use (also in broadcast) two different formats of messages: plain with `&` character,
						or modern json format with support of hover and click events.
						""");
				yaml.setComment(defaultNothing.getCurrentPath() + ".pattern", 
						"""
						This is list of commands to execute by console when order will be executed.
						Text formatting is not supported
						""");
				yaml.setComment(defaultVip.getCurrentPath(), 
						"""
						This is extended example of prize order.
						This prize order does not have constant text as pattern,
						so it will match more than one different pending order names.
						""");
				yaml.setComment(defaultVip.getCurrentPath() + ".pattern", 
						"""
						This pattern is more advanced than above, it contains special elements.
						For example pattern below will match string `vip-10d`,
						but ignore `vip-0d`, `vip`, `kebab` and lots of others.
						""");
				yaml.setComment(defaultVip.getCurrentPath() + ".command", 
						"""
						Another benefit of using advanced patterns is
						ability to use specified matched sections
						(between parentheses, ex: '(a*)') as replacements.
						For instance `$1` will be replaced with matcher section with number 1
						""");
				yaml.save();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final Map<String, PrizeOrder> ordersMap = new HashMap<>();
		
		yaml.getKeys(false).forEach(key -> {
			if (yaml.isConfigurationSection(key)) {
				ConfigurationSection cfg = yaml.getConfigurationSection(key);
				if (cfg.isString("pattern")) {
					Pattern pattern = Pattern.compile(cfg.getString("pattern"));
					List<String> message = cfg.getStringList("message");
					List<String> broadcast = cfg.getStringList("broadcast");
					List<String> command = cfg.getStringList("command");
					ordersMap.put(key.toLowerCase(), new PrizeOrder(key, pattern, message, broadcast, command));
				}
			}
		});
		this.orders = Collections.unmodifiableMap(ordersMap);
		
		return this;
	}
	
	public Map<String, PrizeOrder> getOrders() {
		return this.orders;
	}
	
	
	public class PrizeOrder {
		
		private final String name;
		private final Pattern pattern;
		private final List<String> broadcast;
		private final List<String> message;
		private final List<String> command;
		
		private PrizeOrder(@NotNull String name, @NotNull Pattern pattern, @NotNull List<String> broadcast, @NotNull List<String> message, @NotNull List<String> command) {
			this.name = name;
			this.pattern = pattern;
			this.broadcast = Collections.unmodifiableList(broadcast);
			this.message = Collections.unmodifiableList(message);
			this.command = Collections.unmodifiableList(command);
		}
		
		public @NotNull String getName() {
			return this.name;
		}
		
		public @NotNull Pattern getPattern() {
			return this.pattern;
		}
		
		public @NotNull List<String> getBroadcast() {
			return this.broadcast;
		}
		
		public @NotNull List<String> getMessage() {
			return this.message;
		}
		
		public @NotNull List<String> getCommand() {
			return this.command;
		}
		
		public boolean examine(User user, String orderName, String token) {
			final Matcher match = this.pattern.matcher(orderName);
			if (!match.matches()) return false;
			Collection<? extends CommonPlayer> all = plugin.getCommonServer().getPlayers();
			CommonPlayer player = plugin.getCommonServer().getPlayer(user.getUniqueId());
			CommonSender console = plugin.getCommonServer().getConsole();
			UnaryOperator<String> replacer = s -> {
				s = s.replace("%player%", user.getName())
						.replace("%playerId%", user.getUniqueId().toString())
						.replace("%token%", token);
				for (int i = 0; i <= match.groupCount(); i++) s = s.replace("$"+i, escapeJson(match.group(i)));
				return s;
			};
			
			// Broadcast
			this.broadcast.stream().map(replacer).map(MiscUtils::parseComponent).forEach(msg -> all.forEach(p -> p.sendMessage(msg)));
			
			// Message
			this.message.stream().map(replacer).map(MiscUtils::parseComponent).forEach(player::sendMessage);
			
			// Command
			this.command.stream().map(replacer).forEach(console::executeProxyCommand);
			return true;
		}
		
		private String escapeJson(String str) {
			str = new Gson().toJson(str);
			return str.substring(1, str.length()-1);
		}
		
	}
	

}
