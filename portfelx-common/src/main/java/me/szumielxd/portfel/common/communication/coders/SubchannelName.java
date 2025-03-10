package me.szumielxd.portfel.common.communication.coders;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SubchannelName {
	
	REGISTER("Register"),
	
	BUY("Buy"),
	MINORECO_GIVE("MinorGive"),
	MINORECO_TAKE("MinorTake"),
	TOKEN("Token"),
	
	USER_INFO("UserInfo"),
	SERVER_INFO("ServerInfo"),
	TOP_INFO("TopInfo");
	
	@Getter private final @NotNull String name;
	
	
	public static @NotNull Optional<SubchannelName> getByName(@NotNull String name) {
		return Stream.of(values())
				.filter(e -> e.getName().equals(name))
				.findFirst();
	}
	

}
