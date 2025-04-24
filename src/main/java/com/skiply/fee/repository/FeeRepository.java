package com.skiply.fee.repository;

import com.skiply.fee.model.FeeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeRepository extends JpaRepository<FeeTransaction, Long> {
    List<FeeTransaction> findByStudentId(String studentId);

    Optional<FeeTransaction> findByReferenceNumber(String referenceNumber);
}