package com.learn.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringBootTest
public class NeededInfoTest {

    private static final String metadata = "src/test/resources/model/metadata_16.json";

    @Test
    public void getDateReference() {
        String date = "";
        try {
            String fileJson = new String(Files.readAllBytes(Paths.get(metadata)));
            Pattern pattern = Pattern.compile("\"reference_date\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(fileJson);

            if (matcher.find()) {
                String referenceDate = matcher.group(1);
                System.out.println("Date found: " + referenceDate);
                date = referenceDate;
            } else {
                System.out.println("Reference date not found");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertFalse(date.isEmpty(), "The date should not be empty");

    }




 /*
[
  5,        ← day_of_week (sexta)
  6,        ← month (junho)
  14,       ← day_of_month
  112,      ← days_since_reference
  4.57,     ← rolling_avg_7 media de vendas na semana
  4.12,     ← rolling_avg_30 media de vendas no mes
  5.00,     ← lag_1 vendas ontem
  4.80      ← lag_7 vendas semana passada
]*/
}
