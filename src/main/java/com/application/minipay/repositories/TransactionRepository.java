package com.application.minipay.repositories;

import com.application.minipay.domains.Transaction;
import com.application.minipay.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);
    Page<Transaction> findAllByUserIdAndStatus(UUID userId, Status status, Pageable pageable);
}
