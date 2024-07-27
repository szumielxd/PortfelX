package me.szumielxd.portfel.common.communication.coders;

import java.util.UUID;

import lombok.Getter;

@IdentifiedMessage("Top")
public class TopMessage implements MessagePacket {

	@Getter @MessageEntry(MessageEntryCoders.UUID) private UUID proxyId;
	@Getter @MessageEntry(MessageEntryCoders.OBJECT) private TopUser[] entries;
	
	@MessageObject
	public static class TopUser {
		
		@Getter @MessageEntry(MessageEntryCoders.UUID) private UUID uniqueId;
		@Getter @MessageEntry(MessageEntryCoders.ASCII) private String username;
		@Getter @MessageEntry(MessageEntryCoders.LONG) private long balance;
		
	}
	
	

}
