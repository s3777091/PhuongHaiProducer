package com.example.phuonghaiproducer.Repository;

import com.example.phuonghaiproducer.Entity.ProductEntity;
import com.example.phuonghaiproducer.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByFullName(String name);
}
