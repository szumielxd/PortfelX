package me.szumielxd.portfel.common.communication.coders.messages.setup;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;

@IdentifiedMessage("MinorGive")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistrationRequestMessage implements MessagePacket {
	
	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID operationId;
	@Getter @MessageEntry(MessageEntryCoders.CRYPTO) private @NotNull EncryptedObject<CryptoPayload> data;
	
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class CryptoPayload {
		
		@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID proxyId;
		@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID serverId;
		
	}

}
