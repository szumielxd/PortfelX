package me.szumielxd.portfel.common.communication.coders.messages.info;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;

@IdentifiedMessage("ServerInfo")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ServerInfoMessage implements MessagePacket {
	
	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID serverId;

}
