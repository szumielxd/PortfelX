package me.szumielxd.portfel.common.communication.coders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.common.communication.coders.messages.TopMessage;
import me.szumielxd.portfel.common.communication.coders.messages.TopMessage.TopUser;

class CodersTest {
	
	
	@Test
	void simpleTopMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var player1 = UUID.fromString("00000000-2222-3333-4444-555555555555");
		var message = new TopMessage(id, new TopUser[] {
					new TopUser(player1, "player1", 10),
					new TopUser(player1, "player2", 1)
				});
		var bout = ByteStreams.newDataOutput();
		PacketCoder.encode(bout, message);
		var bin = ByteStreams.newDataInput(bout.toByteArray());
		var result = PacketCoder.decode(bin, TopMessage.class);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTopMessageConvertionEmptyEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var message = new TopMessage(id, new TopUser[] {});
		var bout = ByteStreams.newDataOutput();
		PacketCoder.encode(bout, message);
		var bin = ByteStreams.newDataInput(bout.toByteArray());
		var result = PacketCoder.decode(bin, TopMessage.class);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTopMessageConvertionNotEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var player1 = UUID.fromString("00000000-2222-3333-4444-555555555555");
		var message = new TopMessage(id, new TopUser[] {
					new TopUser(player1, "player1", 10)
				});
		var notMessage = new TopMessage(id, new TopUser[] {
				new TopUser(player1, "player", 10)
			});
		var bout = ByteStreams.newDataOutput();
		PacketCoder.encode(bout, message);
		var bin = ByteStreams.newDataInput(bout.toByteArray());
		var result = PacketCoder.decode(bin, TopMessage.class);
		assertTrue(result.isPresent());
		assertNotEquals(notMessage, result.get());
	}
	

}
