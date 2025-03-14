package me.szumielxd.portfel.common.communication.coders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.common.io.ByteStreams;

import me.szumielxd.portfel.api.enums.EcoType;
import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.common.communication.coders.messages.common.UserIdentifier;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoGiveResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.MinorEcoTakeResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TokenTransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.eco.TransactionResultMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.ServerInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopMessage.TopUser;
import me.szumielxd.portfel.common.communication.coders.messages.info.TopRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.UserInfoMessage;
import me.szumielxd.portfel.common.communication.coders.messages.info.UserInfoRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationRequestMessage;
import me.szumielxd.portfel.common.communication.coders.messages.setup.RegistrationResultMessage;

class CodersTest {
	
	
	@Test
	void simpleTopMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var player1 = UUID.fromString("00000000-2222-3333-4444-555555555555");
		var message = new TopMessage(id, EcoType.MAIN, new TopUser[] {
					new TopUser(new UserIdentifier(player1, "player1"), 10),
					new TopUser(new UserIdentifier(player1, "player2"), 1)
				});
		var result = reconstruct(TopMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTopMessageConvertionEmptyEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var message = new TopMessage(id, EcoType.MAIN, new TopUser[] {});
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
		var message = new TopMessage(id, EcoType.MAIN, new TopUser[] {
					new TopUser(new UserIdentifier(player1, "player1"), 10)
				});
		var notMessage = new TopMessage(id, EcoType.MAIN, new TopUser[] {
				new TopUser(new UserIdentifier(player1, "player"), 10)
			});
		var bout = ByteStreams.newDataOutput();
		PacketCoder.encode(bout, message);
		var bin = ByteStreams.newDataInput(bout.toByteArray());
		var result = PacketCoder.decode(bin, TopMessage.class);
		assertTrue(result.isPresent());
		assertNotEquals(notMessage, result.get());
	}
	
	@Test
	void simpleServerInfoMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var message = new ServerInfoMessage(id);
		var result = reconstruct(ServerInfoMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTopRequestMessageConvertionEqual() {
		var message = new TopRequestMessage(EcoType.MINOR);
		var result = reconstruct(TopRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleUserInfoMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var userId = UUID.fromString("00000000-2222-3333-4444-555555555555");
		var message = new UserInfoMessage(id, new UserIdentifier(userId, "test_user"), 10, 0, false);
		var result = reconstruct(UserInfoMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleUserInfoRequestMessageConvertionEqual() {
		var message = new UserInfoRequestMessage();
		var result = reconstruct(UserInfoRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleRegistrationRequestMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var pid = UUID.fromString("11111111-1111-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new RegistrationRequestMessage(id,
				new EncryptedObject<>(
						RegistrationRequestMessage.CryptoPayload.class,
						new RegistrationRequestMessage.CryptoPayload(pid, sid),
						key));
		var result = reconstruct(RegistrationRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleRegistrationResultMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var pid = UUID.fromString("11111111-1111-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new RegistrationResultMessage(id,
				new EncryptedObject<>(
						RegistrationResultMessage.CryptoPayload.class,
						new RegistrationResultMessage.CryptoPayload(RegistrationResultMessage.RegistrationStatus.OK, pid, sid),
						key));
		var result = reconstruct(RegistrationResultMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleMinorEcoGiveRequestMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new MinorEcoGiveRequestMessage(id,
				new EncryptedObject<>(
						MinorEcoGiveRequestMessage.CryptoPayload.class,
						new MinorEcoGiveRequestMessage.CryptoPayload(sid, 10),
						key));
		var result = reconstruct(MinorEcoGiveRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleMinorEcoGiveResultMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new MinorEcoGiveResultMessage(id,
				new EncryptedObject<>(
						MinorEcoGiveResultMessage.CryptoPayload.class,
						new MinorEcoGiveResultMessage.CryptoPayload(sid, 10, TransactionStatus.OK, ""),
						key));
		var result = reconstruct(MinorEcoGiveResultMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleMinorEcoTakeRequestMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new MinorEcoTakeRequestMessage(id,
				new EncryptedObject<>(
						MinorEcoTakeRequestMessage.CryptoPayload.class,
						new MinorEcoTakeRequestMessage.CryptoPayload(sid, 10),
						key));
		var result = reconstruct(MinorEcoTakeRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleMinorEcoTakeResultMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new MinorEcoTakeResultMessage(id,
				new EncryptedObject<>(
						MinorEcoTakeResultMessage.CryptoPayload.class,
						new MinorEcoTakeResultMessage.CryptoPayload(sid, 10, TransactionStatus.OK, ""),
						key));
		var result = reconstruct(MinorEcoTakeResultMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTokenTransactionRequestMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new TokenTransactionRequestMessage(id,
				new EncryptedObject<>(
						TokenTransactionRequestMessage.CryptoPayload.class,
						new TokenTransactionRequestMessage.CryptoPayload(sid, "7266475", "vip", 1),
						key));
		var result = reconstruct(TokenTransactionRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTransactionRequestMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new TransactionRequestMessage(id,
				new EncryptedObject<>(
						TransactionRequestMessage.CryptoPayload.class,
						new TransactionRequestMessage.CryptoPayload(sid, 123, "Portfel", "vip"),
						key));
		var result = reconstruct(TransactionRequestMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	@Test
	void simpleTransactionResultMessageConvertionEqual() {
		var id = UUID.fromString("11111111-2222-3333-4444-555555555555");
		var sid = UUID.fromString("11111111-0000-3333-4444-555555555555");
		var key = "asdfghjk";
		var message = new TransactionResultMessage(id,
				new EncryptedObject<>(
						TransactionResultMessage.CryptoPayload.class,
						new TransactionResultMessage.CryptoPayload(sid, 123, TransactionStatus.OK, 0, "vip"),
						key));
		var result = reconstruct(TransactionResultMessage.class, message);
		assertTrue(result.isPresent());
		assertEquals(message, result.get());
	}
	
	
	
	private <T extends MessagePacket> Optional<T> reconstruct(Class<T> clazz, T message) {
		var bout = ByteStreams.newDataOutput();
		PacketCoder.encode(bout, message);
		var bin = ByteStreams.newDataInput(bout.toByteArray());
		return PacketCoder.decode(bin, clazz);
	}
	

}
