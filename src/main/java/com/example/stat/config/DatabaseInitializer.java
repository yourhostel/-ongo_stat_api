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
            // Загрузка данных из JSON файла
            InputStream inputStream = new ClassPathResource("test_report.json").getInputStream();
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Преобразование JSON в объекты Java
            ObjectMapper mapper = new ObjectMapper();
            SalesReport report = mapper.readValue(json, SalesReport.class);

            // Сохранение данных в MongoDB
            // Здесь вы сохраните данные в соответствующих коллекциях
            mongoTemplate.save(report, "collectionName");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
