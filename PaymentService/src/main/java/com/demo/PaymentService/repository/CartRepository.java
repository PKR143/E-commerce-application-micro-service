package com.demo.PaymentService.repository;


import com.demo.PaymentService.entity.UserCartDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<UserCartDetailsEntity,String> {
}

