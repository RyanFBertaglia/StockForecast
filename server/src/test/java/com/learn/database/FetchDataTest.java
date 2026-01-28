package com.learn.database;

import com.learn.model.Product;
import com.learn.repository.ProductRepository;
import com.learn.repository.SellsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class FetchDataTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SellsRepository sellsRepository;

    LocalDate baseDate = LocalDate.of(2025, 12, 20);
    Long idProduct = 2L;

    @Test
    public void getAnProduct() {
        Product product = productRepository.findById(1L).orElse(null);
        System.out.println(product);
        assertThat(product).isNotNull();
    }

    @Test
    public void getProductSells() {
        Integer quantitySold = sellsRepository.countByProductId(idProduct);
        System.out.println(quantitySold);
        assertThat(quantitySold).isGreaterThan(0);
    }

    @Test
    public void getSellsInRange() {
        String start = baseDate.with(DayOfWeek.MONDAY).toString();
        String end = baseDate.with(DayOfWeek.SUNDAY).toString();

        Long result = sellsRepository.countSellsInRange(1L, start, end);
        System.out.println(result);
        assertThat(result).isNotZero();
    }

    @Test
    public void getWeekSells() {
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, baseDate.toString(), baseDate.plusDays(7).toString());
        double avg = (double) quantitySold / 7;
        System.out.println(avg);
        assertThat(avg)
                .as("Each day of the month has sells about to %s", quantitySold)
                .isGreaterThan(0);
    }

    @Test
    public void getMonthSells() {
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, baseDate.toString(), baseDate.plusMonths(1).toString());
        double avg = (double) quantitySold / 7;
        System.out.println(avg);
        assertThat(avg)
                .as("Each day of the month has sells about to %s", quantitySold)
                .isGreaterThan(0);
    }

    @Test
    public void sellsYesterday() {
        String yesterday = baseDate.minusDays(1).toString();
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, yesterday, yesterday);
        System.out.println(quantitySold);
        assertThat(quantitySold)
                .as("Yesterday sells equals to %s", quantitySold)
                .isGreaterThan(0);
    }

    @Test
    public void sellsLastWeek() {
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, baseDate.minusWeeks(1).toString(), baseDate.toString());
        System.out.println(quantitySold);
        assertThat(quantitySold)
                .as("Last week sells on the same day was equals to %s", quantitySold)
                .isGreaterThan(0);
    }
}