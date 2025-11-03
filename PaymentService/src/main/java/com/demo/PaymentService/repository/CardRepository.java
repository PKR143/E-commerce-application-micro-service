package com.demo.PaymentService.repository;

import com.demo.PaymentService.entity.CardDetailsEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardDetailsEntity,String> {
    Optional<CardDetailsEntity> findByCardNumberAndUsername(@Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits") String cardNumber, @NotBlank(message = "Username is required for transaction") String username);
}
