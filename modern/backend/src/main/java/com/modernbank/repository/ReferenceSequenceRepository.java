package com.modernbank.repository;

import com.modernbank.domain.ReferenceSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceSequenceRepository extends JpaRepository<ReferenceSequence, Long> {
}
