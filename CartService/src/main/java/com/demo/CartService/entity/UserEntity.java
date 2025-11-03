package com.demo.CartService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user_entity")
public class  UserEntity{
    @Column( name = "user_id")
    private String id;
    private String firstName ;
    private String lastName;
    private String mail;
    private String mobileNo;
    private String address;
    @Id
    @Column(name="username")
    private String username;
    private String password ;
    private String role ;

//    private String providerId;
//
//    @Enumerated(EnumType.STRING)
//    private AuthProviderType ProviderType;
}