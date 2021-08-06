package me.szumielxd.portfel.bukkit.objects;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.portfel.common.objects.User;

public class Transaction {
	
	private final User user;
	private final UUID operationId;
	
	private final List<String> broadcast;
	private final List<String> message;
	private final List<String> command;
	
	private TransactionResult result = null;
	
	public Transaction(@NotNull User user, @NotNull UUID operationId, @NotNull String orderName, @NotNull List<String> broadcast, @NotNull List<String> message, @NotNull List<String> command) {
		this.user = user;
		this.operationId = operationId;
		this.broadcast = broadcast;
		this.message = message;
		this.command = command;
	}
	
	
	public boolean finish(@NotNull TransactionResult result) {
		if (result != null) return false;
		this.result = result;
		
		((BukkitOperableUser)this.user).setPlainBalance(this.result.newBalance);
		
		// TODO: end this point
		
		return true;
	}
	
	
	public static class TransactionResult {
		
		private final UUID transactionId;
		private final String status;
		private final int globalOrders;
		private final long newBalance;
		
		
		public TransactionResult(@NotNull UUID transactionId, @NotNull String status, int globalOrders, long newBalance) {
			this.transactionId = transactionId;
			this.status = status;
			this.globalOrders = globalOrders;
			this.newBalance = newBalance;
		}
		
		public @NotNull UUID getTransactionId() {
			return this.transactionId;
		}
		
		public @NotNull String getStatus() {
			return this.status;
		}
		
		public int getGlobalOrdersCount() {
			return this.globalOrders;
		}
		
		public long getNewBalance() {
			return this.newBalance;
		}
		
	}
	

}
