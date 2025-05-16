package hsbc.hw.transaction.system.service;

import hsbc.hw.transaction.system.dto.TransactionRequest;
import hsbc.hw.transaction.system.dto.TransactionResponse;
import hsbc.hw.transaction.system.enums.TransactionType;
import hsbc.hw.transaction.system.exception.TransactionNotFoundException;
import hsbc.hw.transaction.system.model.Transaction;
import hsbc.hw.transaction.system.repository.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private InMemoryTransactionRepository repository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequest sampleRequest;
    private Transaction sampleTransaction;
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;

    @BeforeEach
    void setUp() {
        // 重置所有mock
        reset(repository);
        
        sampleRequest = createSampleTransactionRequest();
        sampleTransaction = createSampleTransaction();
        
        // 创建测试数据，使用不同的时间戳
        transaction1 = createSampleTransaction("user1", LocalDateTime.now().minusDays(2));
        transaction2 = createSampleTransaction("user2", LocalDateTime.now().minusDays(1));
        transaction3 = createSampleTransaction("user3", LocalDateTime.now());
    }

    @Test
    void createTransaction_ShouldSucceed() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = transactionService.createTransaction(sampleRequest);
        
        assertNotNull(response);
        assertEquals(sampleTransaction.getId(), response.getId());
        assertEquals(sampleTransaction.getAmount(), response.getAmount());
        verify(repository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ShouldThrowException_WhenIdIsEmpty() {
        sampleRequest.setId("");
        
        assertThrows(IllegalArgumentException.class, () -> 
            transactionService.createTransaction(sampleRequest)
        );
        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ShouldThrowException_WhenIdIsNull() {
        sampleRequest.setId(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            transactionService.createTransaction(sampleRequest)
        );
        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() {
        when(repository.findById(anyString())).thenReturn(Optional.of(sampleTransaction));

        TransactionResponse response = transactionService.getTransactionById(sampleTransaction.getId());
        
        assertNotNull(response);
        assertEquals(sampleTransaction.getId(), response.getId());
        verify(repository, times(1)).findById(sampleTransaction.getId());
    }

    @Test
    void getTransactionById_ShouldThrowException_WhenNotFound() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> 
            transactionService.getTransactionById("non-existent-id")
        );
        verify(repository, times(1)).findById("non-existent-id");
    }

    @Test
    void getTransactionsByUserId_ShouldReturnAllUserTransactions() {
        String userId = "user123";
        List<Transaction> transactions = Arrays.asList(
            createSampleTransaction(userId),
            createSampleTransaction(userId)
        );
        when(repository.findByUserId(userId)).thenReturn(transactions);

        List<TransactionResponse> responses = transactionService.getTransactionsByUserId(userId);

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getUserId().equals(userId)));
        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    void getAllTransactions_ShouldReturnFirstPage() {
        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(repository.findAll()).thenReturn(allTransactions);

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("timestamp").descending());
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_ShouldReturnLastPage() {
        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(repository.findAll()).thenReturn(allTransactions);

        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by("timestamp").descending());
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_ShouldReturnEmptyPage() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("timestamp").descending());
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageRequest);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_ShouldHandlePageSizeLargerThanTotal() {
        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(repository.findAll()).thenReturn(allTransactions);

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("timestamp").descending());
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageRequest);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_ShouldMaintainSortOrder() {
        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(repository.findAll()).thenReturn(allTransactions);

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("timestamp").descending());
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageRequest);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        
        List<TransactionResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertTrue(content.get(i).getCreatedAt().isAfter(content.get(i + 1).getCreatedAt()) ||
                    content.get(i).getCreatedAt().isEqual(content.get(i + 1).getCreatedAt()));
        }
        
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_ShouldUseCache() {
        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(repository.findAll()).thenReturn(allTransactions);

        // 第一次调用
        Page<TransactionResponse> firstCall = transactionService.getAllTransactions(
            PageRequest.of(0, 20, Sort.by("timestamp").descending())
        );
        
        // 第二次调用
        Page<TransactionResponse> secondCall = transactionService.getAllTransactions(
            PageRequest.of(0, 20, Sort.by("timestamp").descending())
        );
        
        // 验证结果相同
        assertEquals(firstCall.getContent(), secondCall.getContent());
        // 验证数据库被调用了两次（因为缓存被禁用）
        verify(repository, times(2)).findAll();
    }

    @Test
    void getTransactionById_ShouldUseCache() {
        when(repository.findById(anyString())).thenReturn(Optional.of(sampleTransaction));

        // 第一次调用
        TransactionResponse firstCall = transactionService.getTransactionById(sampleTransaction.getId());
        
        // 第二次调用
        TransactionResponse secondCall = transactionService.getTransactionById(sampleTransaction.getId());
        
        // 验证结果相同
        assertEquals(firstCall, secondCall);
        // 验证数据库被调用了两次（因为缓存被禁用）
        verify(repository, times(2)).findById(sampleTransaction.getId());
    }

    @Test
    void getTransactionsByUserId_ShouldUseCache() {
        String userId = "user123";
        List<Transaction> transactions = Arrays.asList(
            createSampleTransaction(userId),
            createSampleTransaction(userId)
        );
        when(repository.findByUserId(userId)).thenReturn(transactions);

        // 第一次调用
        List<TransactionResponse> firstCall = transactionService.getTransactionsByUserId(userId);
        
        // 第二次调用
        List<TransactionResponse> secondCall = transactionService.getTransactionsByUserId(userId);
        
        // 验证结果相同
        assertEquals(firstCall, secondCall);
        // 验证数据库被调用了两次（因为缓存被禁用）
        verify(repository, times(2)).findByUserId(userId);
    }

    @Test
    void updateTransaction_ShouldEvictCache() {
        when(repository.findById(anyString())).thenReturn(Optional.of(sampleTransaction));
        when(repository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        // 先获取一次数据
        TransactionResponse firstCall = transactionService.getTransactionById(sampleTransaction.getId());
        
        // 更新交易
        TransactionResponse updated = transactionService.updateTransaction(sampleTransaction.getId(), sampleRequest);
        
        // 再次获取数据
        TransactionResponse secondCall = transactionService.getTransactionById(sampleTransaction.getId());
        
        // 验证结果相同（因为缓存被禁用）
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall.getId(), secondCall.getId());
        assertEquals(firstCall.getUserId(), secondCall.getUserId());
        assertEquals(firstCall.getAmount(), secondCall.getAmount());
        assertEquals(firstCall.getType(), secondCall.getType());
        assertEquals(firstCall.getTransactionSummary(), secondCall.getTransactionSummary());
        assertEquals(firstCall.getCounterpartyName(), secondCall.getCounterpartyName());
        assertEquals(firstCall.getCounterpartyAccountNumber(), secondCall.getCounterpartyAccountNumber());
        assertEquals(firstCall.getDescription(), secondCall.getDescription());
        
        // 验证数据库被调用了三次
        verify(repository, times(3)).findById(sampleTransaction.getId());
    }

    @Test
    void deleteTransaction_ShouldEvictCache() {
        when(repository.findById(anyString())).thenReturn(Optional.of(sampleTransaction));
        doNothing().when(repository).deleteById(anyString());

        // 先获取一次数据
        TransactionResponse firstCall = transactionService.getTransactionById(sampleTransaction.getId());
        
        // 删除交易
        transactionService.deleteTransaction(sampleTransaction.getId());
        
        // 再次获取数据
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> 
            transactionService.getTransactionById(sampleTransaction.getId())
        );
        
        // 验证数据库被调用了三次
        verify(repository, times(3)).findById(sampleTransaction.getId());
    }

    private TransactionRequest createSampleTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setId("test-id");
        request.setUserId("user123");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.DEPOSIT.name());
        request.setTransactionSummary("Test transaction");
        request.setCounterpartyName("Test Counterparty");
        request.setCounterpartyAccountNumber("1234567890");
        request.setDescription("Test description");
        return request;
    }

    private Transaction createSampleTransaction() {
        return createSampleTransaction("user123", LocalDateTime.now());
    }

    private Transaction createSampleTransaction(String userId) {
        return createSampleTransaction(userId, LocalDateTime.now());
    }

    private Transaction createSampleTransaction(String userId, LocalDateTime timestamp) {
        Transaction transaction = new Transaction();
        transaction.setId("test-id-" + userId);
        transaction.setUserId(userId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setCreatedAt(timestamp);
        transaction.setUpdatedAt(timestamp);
        transaction.setTransactionSummary("Test transaction");
        transaction.setCounterpartyName("Test Counterparty");
        transaction.setCounterpartyAccountNumber("1234567890");
        transaction.setDescription("Test description");
        return transaction;
    }
} 