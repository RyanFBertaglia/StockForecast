package com.learn.controller;

import com.learn.dto.RemainDTO;
import com.learn.service.Prediction;
import com.learn.service.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
public class StockController {

    public StockController(Prediction prediction, ProductInfo productInfo) {
        this.prediction = prediction;
        this.productInfo = productInfo;
    }

    private final Prediction prediction;
    private final ProductInfo productInfo;

    @GetMapping("{id}")
    public ResponseEntity<Long> getStock(@PathVariable long id) {
        return ResponseEntity.ok().body(0L);
    }

    @GetMapping("{id}/remaining")
    public ResponseEntity<RemainDTO> getTimeRemaining(@PathVariable long id) {
        prediction.initialize(id);
        RemainDTO remains = new RemainDTO(id, prediction.forecastDurability(), productInfo.getStock(id));

        return ResponseEntity.ok().body(remains);
    }

}
