package com.demo.PaymentService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_cart_details_entity")
public class UserCartDetailsEntity {

    @Id
    private String username;

    @ManyToMany
    private List<ProductDetailsEntity> items;

    private Double amount;

}

