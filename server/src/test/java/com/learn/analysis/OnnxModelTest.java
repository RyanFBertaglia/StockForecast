package com.learn.analysis;

import ai.onnxruntime.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.FloatBuffer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class OnnxModelTest {
    private static OrtEnvironment env;
    private OrtSession session;

    @BeforeAll
    static void loadEnv() throws OrtException {
        env = OrtEnvironment.getEnvironment();
    }

    @BeforeEach
    void loadModel() throws OrtException {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        session = env.createSession("src/test/resources/model/model_compatible_3.onnx", options);
    }

    @AfterEach
    void closeSession() throws OrtException {
        if (session != null) session.close();
    }

    @Test
    void forecastDurability() throws OrtException {
        float[] features = {2.0f, 5.0f, 25.0f, 30.0f, 120.5f, 115.2f, 110.0f, 105.0f};

        float forecast = inference(features);
        assertTrue(forecast > 0, "Durability must be positive");
        System.out.println("Time left: " + forecast + " days");
    }

    private float inference(float[] inputData) throws OrtException {
        long[] shape = {1, 8};
        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);

        try (OrtSession.Result result = session.run(Map.of("input", tensor))) {
            OnnxValue output = result.get(0);
            return ((float[][]) output.getValue())[0][0];
        }
    }

}
