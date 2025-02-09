package me.szumielxd.portfel.common.communication.coders.messages;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;

@IdentifiedMessage("Top")
@AllArgsConstructor
public class TopMessage implements MessagePacket {

	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID proxyId;
	@Getter @MessageEntry(MessageEntryCoders.OBJECT) private @NotNull TopUser[] entries;
	
	@AllArgsConstructor
	public static class TopUser {
		
		@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID uniqueId;
		@Getter @MessageEntry(MessageEntryCoders.ASCII) private @NotNull String username;
		@Getter @MessageEntry(MessageEntryCoders.LONG) private long balance;
		
	}
	
	

}
