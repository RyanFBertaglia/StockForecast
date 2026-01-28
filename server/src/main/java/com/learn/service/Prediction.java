package com.learn.service;

import ai.onnxruntime.*;
import com.learn.dto.RemainDTO;
import com.learn.exception.ModelNotLoaded;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@NoArgsConstructor
public class Prediction {

    private static OrtEnvironment env;
    private OrtSession session;
    private SellsInfo sells;
    private LocalDate base;
    private long product;
    private LocalDate referenceDate;

    public void initialize(long product) {
        if (env == null) {
            env = OrtEnvironment.getEnvironment();
        }

        this.product = product;
        this.base = LocalDate.now();

        try {
            String modelPath = String.format("src/test/resources/model/model_compatible_%d.onnx", product);
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            session = env.createSession(modelPath, options);
            referenceDate = loadReferenceDate();

        } catch (OrtException e) {
            throw new ModelNotLoaded("Prediction model not found or loaded: " + e.getMessage());
        }
    }

    @Autowired
    public void setSellsInfo(SellsInfo sellsInfo) {
        this.sells = sellsInfo;
    }

    public float forecastDurability() {
        float[] features = {
                base.getDayOfWeek().getValue(),
                base.getMonthValue(),
                base.getDayOfMonth(),
                getDaysSinceReference(),
                sells.getWeekSells(product),
                sells.getMonthSells(product),
                sells.sellsYesterday(product),
                sells.sellsLastWeek(product)
        };

        return inference(features);
    }

    private float inference(float[] inputData) {
        long[] shape = {1, 8};
        OnnxTensor tensor = null;
        try {
            tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
        } catch (OrtException e) {
            throw new RuntimeException(e.getMessage());
        }

        try (OrtSession.Result result = session.run(Map.of("input", tensor))) {
            OnnxValue output = result.get(0);
            return ((float[][]) output.getValue())[0][0];
        } catch (OrtException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private LocalDate loadReferenceDate() {
        String metadataPath = String.format("src/test/resources/model/metadata_%d.json", product);

        try {
            String fileJson = new String(Files.readAllBytes(Paths.get(metadataPath)));
            Pattern pattern = Pattern.compile("\"reference_date\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(fileJson);

            if (matcher.find()) {
                return LocalDate.parse(matcher.group(1));
            }
            throw new ModelNotLoaded("Reference date not found in metadata");

        } catch (Exception e) {
            throw new ModelNotLoaded("Error loading metadata: " + e.getMessage());
        }
    }

    private long getDaysSinceReference() {
        return ChronoUnit.DAYS.between(referenceDate, LocalDate.now());
    }
}