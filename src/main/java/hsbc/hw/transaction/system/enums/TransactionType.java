package hsbc.hw.transaction.system.enums;

/**
 * 交易类型枚举，定义银行系统中常见的交易种类。
 */
public enum TransactionType {

    DEPOSIT("存款", "将资金存入银行账户"),
    WITHDRAWAL("取款", "从银行账户中取出资金"),
    TRANSFER("转账", "将资金从一个账户转移到另一个账户"),
    EXPENSE("消费", "使用银行账户进行购物、支付等消费行为"),
    INCOME("收入", "收到的工资、奖金、利息等收入"),
    PAYMENT("缴费", "缴纳各种费用，如水电费、电话费、物业费等"),
    REFUND("退款", "收到的退款或退货款项");

    private final String label;
    private final String description;

    TransactionType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
