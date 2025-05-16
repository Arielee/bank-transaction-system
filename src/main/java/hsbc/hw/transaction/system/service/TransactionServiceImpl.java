package hsbc.hw.transaction.system.service;

import hsbc.hw.transaction.system.dto.TransactionRequest;
import hsbc.hw.transaction.system.dto.TransactionResponse;
import hsbc.hw.transaction.system.enums.TransactionType;
import hsbc.hw.transaction.system.exception.DuplicateTransactionException;
import hsbc.hw.transaction.system.exception.TransactionNotFoundException;
import hsbc.hw.transaction.system.model.Transaction;
import hsbc.hw.transaction.system.repository.InMemoryTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final InMemoryTransactionRepository repository;

    public TransactionServiceImpl(InMemoryTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    @CacheEvict(value = "transactions", allEntries = true)
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateTransactionRequest(request);
        
        // 检查交易ID是否已存在
        if (repository.findById(request.getId()).isPresent()) {
            throw new DuplicateTransactionException("交易 ID 已存在: " + request.getId());
        }

        Transaction transaction = new Transaction();
        transaction.setId(request.getId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.valueOf(request.getType()));
        transaction.setTransactionSummary(request.getTransactionSummary());
        transaction.setCounterpartyName(request.getCounterpartyName());
        transaction.setCounterpartyAccountNumber(request.getCounterpartyAccountNumber());
        transaction.setDescription(request.getDescription());
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = repository.save(transaction);
        return convertToResponse(savedTransaction);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id", unless = "#result == null")
    public TransactionResponse getTransactionById(String id) {
        log.info("Fetching transaction by ID: {}", id);
        Transaction tx = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", id);
                    return new TransactionNotFoundException("未找到交易记录: " + id);
                });
        log.debug("Transaction found - ID: {}, User: {}, Amount: {}", 
                id, tx.getUserId(), tx.getAmount());
        return TransactionResponse.from(tx);
    }

    @Override
    @Cacheable(value = "transactions", key = "'user:' + #userId", unless = "#result == null")
    public List<TransactionResponse> getTransactionsByUserId(String userId) {
        log.info("Fetching all transactions for user: {}", userId);
        List<Transaction> transactions = repository.findByUserId(userId);
        log.debug("Found {} transactions for user: {}", transactions.size(), userId);
        return transactions.stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "transactions", allEntries = true, beforeInvocation = true)
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        validateTransactionRequest(request);
        
        Transaction existingTransaction = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("交易不存在: " + id));

        // 更新交易信息
        existingTransaction.setUserId(request.getUserId());
        existingTransaction.setAmount(request.getAmount());
        existingTransaction.setType(TransactionType.valueOf(request.getType()));
        existingTransaction.setTransactionSummary(request.getTransactionSummary());
        existingTransaction.setCounterpartyName(request.getCounterpartyName());
        existingTransaction.setCounterpartyAccountNumber(request.getCounterpartyAccountNumber());
        existingTransaction.setDescription(request.getDescription());
        
        // 更新更新时间
        existingTransaction.setUpdatedAt(LocalDateTime.now());

        Transaction updatedTransaction = repository.save(existingTransaction);
        return convertToResponse(updatedTransaction);
    }

    @Override
    @CacheEvict(value = "transactions", allEntries = true, beforeInvocation = true)
    public void deleteTransaction(String id) {
        log.info("Deleting transaction with ID: {}", id);
        if (!repository.findById(id).isPresent()) {
            log.error("Transaction not found for deletion - ID: {}", id);
            throw new TransactionNotFoundException("未找到交易记录: " + id);
        }
        repository.deleteById(id);
        log.info("Transaction deleted successfully - ID: {}", id);
    }

    @Override
    @Cacheable(value = "transactions", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result == null")
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        log.info("Fetching all transactions with pagination - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        List<Transaction> allTransactions = repository.findAll();
        
        // 按时间戳排序
        allTransactions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // 计算分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTransactions.size());
        
        // 获取当前页的数据
        List<Transaction> pageContent = allTransactions.subList(start, end);
        
        // 转换为响应对象
        List<TransactionResponse> responseList = pageContent.stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
        
        log.debug("Found {} transactions for page {}", responseList.size(), pageable.getPageNumber());
        
        return new PageImpl<>(responseList, pageable, allTransactions.size());
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getUserId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType().name());
        response.setTransactionSummary(transaction.getTransactionSummary());
        response.setCounterpartyName(transaction.getCounterpartyName());
        response.setCounterpartyAccountNumber(transaction.getCounterpartyAccountNumber());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (!StringUtils.hasText(request.getId())) {
            log.error("Transaction ID is required");
            throw new IllegalArgumentException("交易ID不能为空");
        }

        log.info("Creating new transaction for user: {} with ID: {}", request.getUserId(), request.getId());

        if (request.getType() == null) {
            log.error("Transaction type is required");
            throw new IllegalArgumentException("交易类型不能为空");
        }
    }
} 