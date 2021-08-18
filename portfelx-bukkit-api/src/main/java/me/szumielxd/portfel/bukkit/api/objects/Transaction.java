package me.szumielxd.portfel.bukkit.api.objects;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.portfel.api.enums.TransactionStatus;
import me.szumielxd.portfel.api.objects.User;
import me.szumielxd.portfel.bukkit.api.objects.OrderData.OrderDataOnAir;

public interface Transaction {
	
	/**
	 * get target of this transaction.
	 * 
	 * @return user related with this transaction
	 */
	public @NotNull User getUser();
	
	/**
	 * Get identifier of this transaction.
	 * 
	 * @return transaction's UUID
	 */
	public @NotNull UUID getTransactionId();
	
	/**
	 * Get order related with this transaction.
	 * 
	 * @return prepared order
	 */
	public @NotNull OrderDataOnAir getOrder();
	
	/**
	 * Get result of this transaction.
	 * 
	 * @return result if transaction is finished, otherwise null
	 */
	public @Nullable TransactionResult getResult();
	
	/**
	 * Finish this transaction.
	 * 
	 * @implNote <b>Thread unsafe</b>
	 * @param result of this transaction
	 * @return true if transaction was successfully finished, otherwise false
	 * @throws RuntimeException if executed in main thread
	 */
	public boolean finish(@NotNull TransactionResult result) throws RuntimeException;
	
	
	public static class TransactionResult {
		
		private final UUID transactionId;
		private final TransactionStatus status;
		private final long newBalance;
		private final int globalOrders;
		private final Throwable throwable;
		
		
		public TransactionResult(@NotNull UUID transactionId, @NotNull TransactionStatus status, long newBalance, int globalOrders, @Nullable Throwable throwable) {
			this.transactionId = transactionId;
			this.status = status;
			this.globalOrders = globalOrders;
			this.newBalance = newBalance;
			this.throwable = throwable;
		}
		
		/**
		 * Get identifier of this transaction.
		 * 
		 * @return transaction's UUID
		 */
		public @NotNull UUID getTransactionId() {
			return this.transactionId;
		}
		
		/**
		 * Get end status of this transaction.
		 * 
		 * @return transaction's status
		 */
		public @NotNull TransactionStatus getStatus() {
			return this.status;
		}
		
		/**
		 * Get new user's balance after this transaction.
		 * 
		 * @return new balance
		 */
		public long getNewBalance() {
			return this.newBalance;
		}
		
		/**
		 * Get amount of successfully executed global orders.
		 * 
		 * @return count of global orders
		 */
		public int getGlobalOrdersCount() {
			return this.globalOrders;
		}
		
		/**
		 * Get optional error occurred whilst executing this transaction.
		 * 
		 * @return throwable, may be null
		 */
		public @Nullable Throwable getThrowable() {
			return this.throwable;
		}
		
	}

}
