package com.modernbank.config;

import com.modernbank.repository.EmployeeRepository;
import com.modernbank.domain.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once at application startup.
 * Migrates plain-text passwords in EMPLOYEES to BCrypt hashes.
 * Idempotent: skips rows whose PASSWORD is already a BCrypt hash (length >= 60).
 */
@Component
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseMigrationRunner(EmployeeRepository employeeRepository,
                                   PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("DatabaseMigrationRunner: checking for plain-text passwords...");
        for (Employee emp : employeeRepository.findAll()) {
            if (emp.getPassword() != null && emp.getPassword().length() < 60) {
                String hashed = passwordEncoder.encode(emp.getPassword());
                emp.setPassword(hashed);
                employeeRepository.save(emp);
                log.info("DatabaseMigrationRunner: migrated password for employee '{}'", emp.getUsername());
            }
        }
        log.info("DatabaseMigrationRunner: password migration complete.");
    }
}
