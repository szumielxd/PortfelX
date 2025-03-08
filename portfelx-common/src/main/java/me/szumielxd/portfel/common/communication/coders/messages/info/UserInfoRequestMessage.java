package me.szumielxd.portfel.common.communication.coders.messages.info;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.common.communication.coders.IdentifiedMessage;
import me.szumielxd.portfel.common.communication.coders.MessagePacket;
import me.szumielxd.portfel.common.communication.coders.SubchannelName;

@IdentifiedMessage(SubchannelName.USER_INFO)
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserInfoRequestMessage implements MessagePacket {

}
