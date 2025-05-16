package hsbc.hw.transaction.system.service;

import hsbc.hw.transaction.system.dto.TransactionRequest;
import hsbc.hw.transaction.system.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    /**
     * 创建新的交易记录
     * @param request 交易请求对象
     * @return 交易响应对象
     */
    TransactionResponse createTransaction(TransactionRequest request);

    /**
     * 根据ID获取交易记录
     * @param id 交易ID
     * @return 交易响应对象
     */
    TransactionResponse getTransactionById(String id);

    /**
     * 根据用户ID查询交易记录
     * @param userId 用户ID
     * @return 交易记录列表
     */
    List<TransactionResponse> getTransactionsByUserId(String userId);

    /**
     * 获取所有交易记录（分页）
     * @param pageable 分页参数
     * @return 分页的交易记录列表
     */
    Page<TransactionResponse> getAllTransactions(Pageable pageable);

    /**
     * 更新交易记录
     * @param id 交易ID
     * @param request 交易请求对象
     * @return 交易响应对象
     */
    TransactionResponse updateTransaction(String id, TransactionRequest request);

    /**
     * 删除交易记录
     * @param id 交易ID
     */
    void deleteTransaction(String id);
}