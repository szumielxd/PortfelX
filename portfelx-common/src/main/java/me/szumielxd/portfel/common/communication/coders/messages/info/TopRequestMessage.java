package me.szumielxd.portfel.common.communication.coders.messages.info;

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

@IdentifiedMessage(SubchannelName.TOP_INFO)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TopRequestMessage implements MessagePacket {
	
	@Getter @MessageEntry(MessageEntryCoders.ENUM) private @NotNull EcoType type;

}
