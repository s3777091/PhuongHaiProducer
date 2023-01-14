package com.example.phuonghaiproducer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhuongHaiProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhuongHaiProducerApplication.class, args);
    }


    @Bean
    public NewTopic registerNewsUser() {
        return new NewTopic("UserRegister", 1 , (short) 1);
    }

    @Bean
    public NewTopic addCatogary() {
        return new NewTopic("addCatogary", 1 , (short) 1);
    }

    @Bean
    public NewTopic addProduct() {
        return new NewTopic("addProduct", 1 , (short) 1);
    }

    @Bean
    public NewTopic DeleteMo() {
        return new NewTopic("deleteMo", 1 , (short) 1);
    }

    @Bean
    public NewTopic AddMo() {
        return new NewTopic("addMo", 1 , (short) 1);
    }

    @Bean
    public NewTopic editingMo() {
        return new NewTopic("EditMo", 1 , (short) 1);
    }

    @Bean
    public NewTopic addBill() {
        return new NewTopic("addBill", 1 , (short) 1);
    }



}
