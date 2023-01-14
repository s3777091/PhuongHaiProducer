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
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, updatable = false)
    long id;

    @Column(name = "productname", length=100)
    String productName;

    @Column(name = "productdes", length=10485760)
    String productDescription;

    @Column(name = "totalin", length=10)
    String totalin;

    @Column(name = "totalout", length=10)
    String totalout;

    @Column(name = "price", length=10)
    String price;

    @Column(name = "firstquality", length=100)
    String firstquality;

    @Column(name = "parentname", length=100)
    String parentname;

    @Column(name = "avaiable", length=10)
    String avaiable;

    @ManyToOne
    ProductCatogary productCatogary;
}
