package com.learn.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "products")
@Entity
@Data
public class Product {
    @Id
    private Long id;
    private String name;
    private Integer stock;
}
