package com.example.phuonghaiproducer.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class MOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "datedelivery")
    private Timestamp dateDelivery;

    @Column(name = "dateexpectedcompletion")
    private Timestamp dateExpectedCompletion;

    @Column(name = "datestartmo")
    private Timestamp dateStartMo;

    @Column(name = "token")
    String controltoken;
    @Column(name = "useroder")
    String userName;
    @ElementCollection
    @CollectionTable(name="listOfComponents")
    Map<String, String> collection;
}


