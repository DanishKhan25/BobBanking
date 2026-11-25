package com.modernbank.domain;

import com.modernbank.repository.AccountRepository;
import com.modernbank.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EntityMappingTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void loadCustomerById() {
        Optional<Customer> customer = customerRepository.findById(1001L);
        assertThat(customer).isPresent();
        assertThat(customer.get().getCustomerName()).isEqualTo("John Anderson");
        assertThat(customer.get().getEmail()).isEqualTo("john.anderson@example.com");
    }

    @Test
    void loadAccountByNumber() {
        Optional<Account> account = accountRepository.findByAccountNumber("1001000001");
        assertThat(account).isPresent();
        assertThat(account.get().getAccountType()).isEqualTo(Account.AccountType.SAVINGS);
        assertThat(account.get().getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(account.get().getCustomer().getCustomerId()).isEqualTo(1001L);
    }

    @Test
    void frozenAccountHasCorrectStatus() {
        Optional<Account> account = accountRepository.findByAccountNumber("1001000008");
        assertThat(account).isPresent();
        assertThat(account.get().getStatus()).isEqualTo(Account.AccountStatus.FROZEN);
    }

    @Test
    void customerHasAccounts() {
        Optional<Customer> customer = customerRepository.findById(1001L);
        assertThat(customer).isPresent();
        assertThat(customer.get().getAccounts()).isNotEmpty();
    }
}
