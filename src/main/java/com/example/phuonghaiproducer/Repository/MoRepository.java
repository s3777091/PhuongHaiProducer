package com.example.phuonghaiproducer.Repository;

import com.example.phuonghaiproducer.Entity.MOrder;
import com.example.phuonghaiproducer.Entity.ProductCatogary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoRepository extends JpaRepository<MOrder,Long> {
    MOrder findById(long id);

    MOrder findByControltoken(String token);
}
