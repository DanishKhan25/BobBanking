package com.modernbank.repository;

import com.modernbank.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomer_CustomerId(Long customerId);

    long countByStatus(Account.AccountStatus status);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal sumAllBalances();

    @Query("SELECT COUNT(DISTINCT a.customer) FROM Account a")
    long countDistinctCustomers();
}
