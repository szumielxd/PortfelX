package me.szumielxd.portfel.common.communication.coders.messages.info;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.api.enums.EcoType;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;
import me.szumielxd.portfel.common.communication.coders.messages.common.UserIdentifier;

@IdentifiedMessage(SubchannelName.TOP_INFO)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TopMessage implements MessagePacket {

	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID proxyId;
	@Getter @MessageEntry(MessageEntryCoders.ASCII) private @NotNull EcoType type;
	@Getter @MessageEntry(MessageEntryCoders.OBJECT) private @NotNull TopUser[] entries;
	
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class TopUser {
		
		@Getter @MessageEntry(MessageEntryCoders.OBJECT) private @NotNull UserIdentifier user;
		@Getter @MessageEntry(MessageEntryCoders.LONG) private long balance;
		
	}
	
	

}
