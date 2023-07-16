package com.gfg.ewallet.repository;

import com.gfg.ewallet.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    Optional<Transaction> findByTransactionId(String id);
}
