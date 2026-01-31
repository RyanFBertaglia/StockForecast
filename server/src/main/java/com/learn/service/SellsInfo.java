package com.learn.service;

import com.learn.repository.SellsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SellsInfo {

    private final SellsRepository sellsRepository;
    LocalDate baseDate;

    @Autowired
    public SellsInfo(SellsRepository sellsRepository) {
        this.sellsRepository = sellsRepository;
        this.baseDate = LocalDate.now();
    }

    public float getWeekSells(Long idProduct) {
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, baseDate.toString(), baseDate.minusDays(7).toString());
        return (float) quantitySold / 7;
    }

    public float getMonthSells(Long idProduct) {
        Long quantitySold = sellsRepository.countSellsInRange(idProduct, baseDate.toString(), baseDate.plusMonths(1).toString());
        return (float) quantitySold / 7;
    }

    public Integer getProductSells(Long idProduct) {
        return sellsRepository.countByProductId(idProduct);
    }

    public Long sellsYesterday(Long idProduct) {
        String yesterday = baseDate.minusDays(1).toString();
        return sellsRepository.countSellsInRange(idProduct, yesterday, yesterday);
    }

    public Long sellsLastWeek(Long idProduct) {
        return sellsRepository.countSellsInRange(idProduct, baseDate.minusWeeks(1).toString(), baseDate.toString());
    }

}
