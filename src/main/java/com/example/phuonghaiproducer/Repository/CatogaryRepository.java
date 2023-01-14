package com.example.phuonghaiproducer.Repository;

import com.example.phuonghaiproducer.Entity.ProductCatogary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatogaryRepository extends JpaRepository<ProductCatogary,Long> {
    ProductCatogary findByProducttype(String type);
}
