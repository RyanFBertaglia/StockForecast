package com.learn.controller;

import com.learn.model.Product;
import com.learn.service.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    ProductInfo productInfo;

    @Autowired
    public ProductController (ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<String> getProduct(@PathVariable Long id) {
        Product product = productInfo.getProduct(id);
        return ResponseEntity.ok(product.getName());
    }
}
