package hsbc.hw.transaction.system.dto;

import hsbc.hw.transaction.system.model.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String type;
    private String typeName;
    private String transactionSummary;
    private String counterpartyName;
    private String counterpartyAccountNumber;
    private String description;
    /**
     * 交易创建时间，精确到毫秒。
     */
    private LocalDateTime createdAt;
    /**
     * 交易最后更新时间，精确到毫秒。
     */
    private LocalDateTime updatedAt;

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getUserId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType().name());
        response.setTypeName(transaction.getType().getLabel());
        response.setTransactionSummary(transaction.getTransactionSummary());
        response.setCounterpartyName(transaction.getCounterpartyName());
        response.setCounterpartyAccountNumber(transaction.getCounterpartyAccountNumber());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}