package com.example.phuonghaiproducer.Request;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProductResquest {
    String productName;
    String productDescription;
    String quality;
    String price;
    String parent;
    String type;
}
