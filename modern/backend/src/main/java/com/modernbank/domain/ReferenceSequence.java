package com.modernbank.domain;

import jakarta.persistence.*;

/**
 * Maps to the REFERENCE_SEQUENCE table.
 * Used to generate sequential reference numbers for transactions
 * (DEP/WDR/TRF prefix + 10-digit zero-padded REF_ID).
 */
@Entity
@Table(name = "REFERENCE_SEQUENCE")
public class ReferenceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REF_ID")
    private Long refId;

    public ReferenceSequence() {}

    public Long getRefId() { return refId; }
}
