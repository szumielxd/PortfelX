package me.szumielxd.portfel.common.communication.coders.messages;

import java.util.UUID;

import lombok.Getter;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;

@IdentifiedMessage("Top")
public class TopMessage implements MessagePacket {

	@Getter @MessageEntry(MessageEntryCoders.UUID) private UUID proxyId;
	@Getter @MessageEntry(MessageEntryCoders.OBJECT) private TopUser[] entries;
	
	public static class TopUser {
		
		@Getter @MessageEntry(MessageEntryCoders.UUID) private UUID uniqueId;
		@Getter @MessageEntry(MessageEntryCoders.ASCII) private String username;
		@Getter @MessageEntry(MessageEntryCoders.LONG) private long balance;
		
	}
	
	

}
