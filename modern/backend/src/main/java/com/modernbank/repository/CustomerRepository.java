package com.modernbank.repository;

import com.modernbank.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c JOIN c.accounts a WHERE a.accountNumber = :accountNumber")
    Optional<Customer> findByAccountNumber(@Param("accountNumber") String accountNumber);
}
