package com.example.stat.config;

import com.example.stat.model.report.SalesReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class DatabaseInitializer {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DatabaseInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        try {
            // Loading data from a JSON file
            InputStream inputStream = new ClassPathResource("test_report.json").getInputStream();
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Загрузка данных из JSON файла
            ObjectMapper mapper = new ObjectMapper();
            SalesReport report = mapper.readValue(json, SalesReport.class);

            // Storing Data in MongoDB
            // Here we save the data in the appropriate collections
            mongoTemplate.save(report, "report");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
