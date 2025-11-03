package com.demo.PaymentService.repository;

import com.demo.PaymentService.entity.TransactionStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<TransactionStatusEntity,Long> {
}
