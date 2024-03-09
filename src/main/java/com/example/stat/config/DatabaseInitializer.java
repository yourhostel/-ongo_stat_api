package com.example.stat.config;

import com.example.stat.util.HashUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
public class DatabaseInitializer {
    private static final Logger logger = LogManager
            .getLogger(DatabaseInitializer.class);

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        // Check if the collection exists
        if (!mongoTemplate.collectionExists("report")) {
            try {
                // Loading data from a JSON file
                InputStream inputStream = new ClassPathResource("test_report.json").getInputStream();
                String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                // Convert JsonNode to Document
                ObjectMapper mapper = new ObjectMapper();

                JsonNode rootNode = mapper.readTree(json);

                // Convert JsonNode to Document
                Document report = Document.parse(rootNode.toString());

                // Storing Data in MongoDB
                // Here we save the data in the appropriate collections
                mongoTemplate.save(report, "report");
                saveInitialHash(json);
            } catch (IOException e) {
                logger.error("An error occurred while reading JSON file or saving data to MongoDB", e);
            }
        } else {
            // Collection already exists
            logger.info("The 'report' collection already exists. Initialization is skipped.");
        }
    }

    // Logic for storing hash sum
    private void saveInitialHash(String json) {
        // Calculate the hash sum from a string JSON
        String hash = HashUtil.calculateHash(json);
        // Create a document
        Document hashDocument = new Document("id", "reportHash").append("hash", hash);
        // Save the document to the collection "metadata"
        mongoTemplate.save(hashDocument, "metadata");
    }

}
