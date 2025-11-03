package com.demo.CartService.repository;

import com.demo.CartService.entity.UserCartDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<UserCartDetailsEntity,String> {
}

