package hsbc.hw.transaction.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import hsbc.hw.transaction.system.enums.TransactionType;
import lombok.Data;
/**
 * 交易实体类，用于表示一次银行交易记录。
 */
@Data
public class Transaction {

    /**
     * 交易唯一标识符，使用 UUID确保唯一性。分布式场景下也可以用雪花算法生成
     */
    private String id;

    /**
     * 用户标识符，用于关联交易与用户账户。
     */
    private String userId;

    /**
     * 交易金额，使用 BigDecimal 确保精度无损。
     */
    private BigDecimal amount;

    /**
     * 交易类型，参考 TransactionType 枚举。
     */
    private TransactionType type;

    /**
     * 交易摘要，描述交易性质，如"转账"、"取现"、"汇入"等。
     */
    private String transactionSummary;

    /**
     * 交易对方姓名，用于识别资金往来对象。
     */
    private String counterpartyName;

    /**
     * 交易对方账户号码，用于对账和查询。
     */
    private String counterpartyAccountNumber;

    /**
     * 交易描述或备注信息（可选）。
     */
    private String description;

    /**
     * 交易创建时间，精确到毫秒。
     */
    private LocalDateTime createdAt;

    /**
     * 交易最后更新时间，精确到毫秒。
     */
    private LocalDateTime updatedAt;
}
