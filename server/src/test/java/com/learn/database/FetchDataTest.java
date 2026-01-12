package com.learn.database;

import com.learn.Model.Product;
import com.learn.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class FetchDataTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void getAnProduct() {
        Product product = productRepository.findById(1L).orElse(null);
        System.out.println(product);
        assertThat(product).isNotNull();
    }
}
