package hsbc.hw.transaction.system.enums;
/**
 * 交易状态枚举,后期扩展备用
 */
public enum TransactionStatus {

    CREATED("已创建", "交易已创建，尚未开始处理"),
    PROCESSING("处理中", "交易正在进行中，尚未完成"),
    COMPLETED("已完成", "交易已成功完成并生效"),
    CANCELLED("已取消", "交易已被用户或系统取消"),
    FAILED("失败", "交易因异常或错误导致执行失败");

    private final String label;
    private final String description;

    TransactionStatus(String label, String description) {
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
