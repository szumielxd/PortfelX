package me.szumielxd.portfel.common.communication.coders.messages.eco;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.common.communication.coders.EncryptedObject;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;

@IdentifiedMessage(SubchannelName.MINORECO_TAKE)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MinorEcoTakeResultMessage implements MessagePacket {
	
	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID proxyId;
	@Getter @MessageEntry(MessageEntryCoders.CRYPTO) private @NotNull EncryptedObject<CryptoPayload> data;
	
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@EqualsAndHashCode
	public static class CryptoPayload {
		
		@Getter @MessageEntry(MessageEntryCoders.UTF) private @NotNull String transactionId;
		@Getter @MessageEntry(MessageEntryCoders.LONG) private @NotNull long newBalance;
		@Getter @MessageEntry(MessageEntryCoders.ENUM) private @NotNull TransactionStatus status;
		@Getter @MessageEntry(MessageEntryCoders.UTF) private @NotNull String error;
		
	}

}
