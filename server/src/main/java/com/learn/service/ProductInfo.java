package com.learn.service;

import com.learn.model.Product;
import com.learn.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductInfo {

    private final ProductRepository products;

    @Autowired
    public ProductInfo(ProductRepository products) {
        this.products = products;
    }

    public Product getProduct(Long id) {
        return products.findById(id).orElseThrow
                (() -> new RuntimeException("Product not found"));
    }

    public Integer getStock(long id) {
        Product product = getProduct(id);
        return product.getStock();
    }
}
