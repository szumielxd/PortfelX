package me.szumielxd.portfel.common.communication.coders.messages.common;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.szumielxd.portfel.common.communication.coders.MessageEntry;
import me.szumielxd.portfel.common.communication.coders.MessageEntryCoders;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserIdentifier {

	@Getter @MessageEntry(MessageEntryCoders.UUID) private @NotNull UUID uniqueId;
	@Getter @MessageEntry(MessageEntryCoders.ASCII) private @NotNull String username;

}
