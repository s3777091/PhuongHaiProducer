package com.example.phuonghaiproducer.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ProductCatogary {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "productcatagoryid", nullable = false, updatable = false)
    private long id;

    @Column(name = "producttype", nullable = false, length = 20)
    private String producttype;
}
