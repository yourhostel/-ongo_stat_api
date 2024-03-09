package com.example.stat.service;

import com.example.stat.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class DataUpdateService {
    private static final Logger logger = LogManager
            .getLogger(DataUpdateService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedDelay = 10000) // 300000 ms = 5 minutes
    public void updateDataFromJsonFile() throws IOException {
        try {
            InputStream inputStream = new ClassPathResource("test_report.json").getInputStream();
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String newHash = HashUtil.calculateHash(json);
            Query query = new Query(Criteria.where("id").is("reportHash"));
            Document currentHashDocument = mongoTemplate
                    .findOne(query, Document.class, "metadata");

            if (currentHashDocument != null) {
                String currentHash = currentHashDocument.getString("hash");
                logger.info("current hash ok");
                if (!newHash.equals(currentHash)) {
                    // Updating data and hash in the database
                    updateDatabase(json, newHash);
                    invalidateCache();
                    logger.warn("Update database ok. Invalidate cache ok");
                }
            } else {
                logger.warn("The hash sum is missing from the database. Hash initialization in progress...");
                // Storing data and hash in database if hash is missing
                updateDatabase(json, newHash);
                invalidateCache();
            }
        } catch (Exception e) {
            logger.error("Error updating data from file: {} | {}", e.getMessage(),  e);
        }
    }

    private void updateDatabase(String json, String newHash) {
        Document newReport = Document.parse(json);
        mongoTemplate.dropCollection("report");
        mongoTemplate.save(newReport, "report");

        Query query = new Query(Criteria.where("id").is("reportHash"));
        Update update = new Update().set("hash", newHash);
        mongoTemplate.upsert(query, update, "metadata");
    }

    private void invalidateCache() {
        // All associated caches are invalidated
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects
                        .requireNonNull(cacheManager.getCache(cacheName))
                        .clear());
    }

}
