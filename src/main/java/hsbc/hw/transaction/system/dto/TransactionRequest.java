package hsbc.hw.transaction.system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String type;
    private String transactionSummary;
    private String counterpartyName;
    private String counterpartyAccountNumber;
    private String description;
}