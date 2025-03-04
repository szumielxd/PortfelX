package me.szumielxd.portfel.common.communication.coders;

public interface MessagePacket {
	
	
	public default byte[] toBytePacket() {
		return PacketCoder.encodePacket(this);
	}
	

}
