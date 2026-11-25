package com.modernbank.repository;

import com.modernbank.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsernameAndStatus(String username, String status);
    Optional<Employee> findByUsername(String username);
}
