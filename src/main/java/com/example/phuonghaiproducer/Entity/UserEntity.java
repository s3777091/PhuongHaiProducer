package com.example.phuonghaiproducer.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "userId", nullable = false, updatable = false)
    private long userId;

    @Column(name = "fullname")
    @NotNull(message = "Full Name cannot be null")
    private String fullName;

    @Size(min = 6, max = 20, message = "Password must be from 6 to 20 characters")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status", columnDefinition = "BOOLEAN")
    private boolean status;


}
