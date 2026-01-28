package com.learn.analysis;

import ai.onnxruntime.OrtException;
import com.learn.service.Prediction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ServiceTest {

    @Autowired
    private Prediction prediction;

    @Test
    void prediction() throws OrtException {
        prediction.initialize(2);
        float daysLeft = prediction.forecastDurability();
        assertFalse(daysLeft < 0, "As there are products left should have more time");
    }

}
