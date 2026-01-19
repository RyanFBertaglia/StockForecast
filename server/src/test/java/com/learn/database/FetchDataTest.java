package com.learn.database;

import com.learn.model.Product;
import com.learn.repository.ProductRepository;
import com.learn.repository.SellsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class FetchDataTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SellsRepository sellsRepository;

    @Test
    void getAnProduct() {
        Product product = productRepository.findById(1L).orElse(null);
        System.out.println(product);
        assertThat(product).isNotNull();
    }

    @Test
    void getProductSells() {
        Long idProduct = 2L;
        Integer quantitySold = sellsRepository.countByProductId(idProduct);
        System.out.println(quantitySold);
        assertThat(quantitySold).isGreaterThan(0);
    }
}
