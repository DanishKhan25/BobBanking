package com.modernbank.service;

import com.modernbank.domain.Account;
import com.modernbank.domain.ReferenceSequence;
import com.modernbank.domain.Transaction;
import com.modernbank.dto.*;
import com.modernbank.exception.BusinessException;
import com.modernbank.repository.AccountRepository;
import com.modernbank.repository.ReferenceSequenceRepository;
import com.modernbank.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000.00");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ReferenceSequenceRepository referenceSequenceRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              ReferenceSequenceRepository referenceSequenceRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.referenceSequenceRepository = referenceSequenceRepository;
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {
        validateAmount(request.getAmount());
        Account account = getActiveAccount(request.getAccountNumber());

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String ref = generateReference("DEP");
        saveTransaction(account, Transaction.TransactionType.DEPOSIT,
                request.getAmount(), ref,
                descriptionOrDefault(request.getDescription(), "Cash deposit"));

        return new TransactionResponse(ref, newBalance);
    }

    @Transactional
    public TransactionResponse withdrawal(TransactionRequest request) {
        validateAmount(request.getAmount());
        Account account = getActiveAccount(request.getAccountNumber());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient funds. Available balance: " +
                    account.getBalance().toPlainString());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String ref = generateReference("WDR");
        saveTransaction(account, Transaction.TransactionType.WITHDRAWAL,
                request.getAmount(), ref,
                descriptionOrDefault(request.getDescription(), "Cash withdrawal"));

        return new TransactionResponse(ref, newBalance);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new BusinessException("Source and destination accounts must be different");
        }
        validateAmount(request.getAmount());

        Account fromAccount = getActiveAccount(request.getFromAccount());
        Account toAccount   = getActiveAccount(request.getToAccount());

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient funds in source account. Available: " +
                    fromAccount.getBalance().toPlainString());
        }

        BigDecimal fromNew = fromAccount.getBalance().subtract(request.getAmount());
        BigDecimal toNew   = toAccount.getBalance().add(request.getAmount());

        fromAccount.setBalance(fromNew);
        toAccount.setBalance(toNew);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String ref = generateReference("TRF");
        String debitDesc  = descriptionOrDefault(request.getDescription(),
                "Transfer to " + request.getToAccount());
        String creditDesc = descriptionOrDefault(request.getDescription(),
                "Transfer from " + request.getFromAccount());

        saveTransaction(fromAccount, Transaction.TransactionType.TRANSFER_DEBIT,
                request.getAmount(), ref, debitDesc);
        saveTransaction(toAccount, Transaction.TransactionType.TRANSFER_CREDIT,
                request.getAmount(), ref, creditDesc);

        return new TransactionResponse(ref, fromNew, toNew);
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryItem> getHistory(String accountNumber, String type) {
        if (!accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new EntityNotFoundException("Account not found: " + accountNumber);
        }

        List<Transaction> transactions;
        if (type != null && !type.isBlank()) {
            Transaction.TransactionType txType;
            try {
                txType = Transaction.TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid transaction type: " + type);
            }
            transactions = transactionRepository.findByAccountNumberAndTypeOrderByDateDesc(accountNumber, txType);
        } else {
            transactions = transactionRepository.findByAccountNumberOrderByDateDesc(accountNumber);
        }

        return transactions.stream().map(this::toHistoryItem).collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Account getActiveAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Account " + accountNumber +
                    " is not active (status: " + account.getStatus() + ")");
        }
        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException("Amount must not exceed 10,000,000");
        }
        if (amount.scale() > 2) {
            throw new BusinessException("Amount must have at most 2 decimal places");
        }
    }

    private String generateReference(String prefix) {
        ReferenceSequence seq = referenceSequenceRepository.save(new ReferenceSequence());
        return String.format("%s%010d", prefix, seq.getRefId());
    }

    private void saveTransaction(Account account, Transaction.TransactionType type,
                                 BigDecimal amount, String ref, String description) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "system";

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setReferenceNumber(ref);
        tx.setDescription(description + " (by " + username + ")");
        tx.setTransactionDate(LocalDateTime.now().format(DATE_FMT));
        transactionRepository.save(tx);
    }

    private String descriptionOrDefault(String desc, String defaultDesc) {
        return (desc != null && !desc.isBlank()) ? desc : defaultDesc;
    }

    private TransactionHistoryItem toHistoryItem(Transaction tx) {
        TransactionHistoryItem item = new TransactionHistoryItem();
        item.setTransactionId(tx.getTransactionId());
        item.setTransactionType(tx.getTransactionType().name());
        item.setAmount(tx.getAmount());
        item.setTransactionDate(tx.getTransactionDate());
        item.setReferenceNumber(tx.getReferenceNumber());
        item.setDescription(tx.getDescription() != null ? tx.getDescription() : "");
        return item;
    }
}
