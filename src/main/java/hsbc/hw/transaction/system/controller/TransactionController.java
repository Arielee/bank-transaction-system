package hsbc.hw.transaction.system.controller;

import hsbc.hw.transaction.system.dto.TransactionRequest;
import hsbc.hw.transaction.system.dto.TransactionResponse;
import hsbc.hw.transaction.system.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")  // 允许跨域请求
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@RequestBody TransactionRequest request) {
        return service.createTransaction(request);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable String id) {
        return service.getTransactionById(id);
    }

    @GetMapping("/user/{userId}")
    public List<TransactionResponse> getByUserId(@PathVariable String userId) {
        return service.getTransactionsByUserId(userId);
    }

    @GetMapping
    public Page<TransactionResponse> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.getAllTransactions(PageRequest.of(page, size, Sort.by("timestamp").descending()));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable String id, @RequestBody TransactionRequest request) {
        return service.updateTransaction(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.deleteTransaction(id);
    }
}

