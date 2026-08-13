package com.shravya.bankingapp.service;

import com.shravya.bankingapp.dto.TransactionResponse;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import com.shravya.bankingapp.dto.TransactionResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getTransactions(Long accountId) {

        List<Transaction> transactions =
                transactionRepository.findByAccountId(accountId);

        List<TransactionResponse> response = new ArrayList<>();

        for(Transaction t : transactions){
            response.add(
                    new TransactionResponse(
                            t.getId(),
                            t.getAmount(),
                            t.getType(),
                            t.getTransactionDate(),
                            t.getAccount().getAccountNumber()
                    )
            );
        }

        return response;
    }
}
