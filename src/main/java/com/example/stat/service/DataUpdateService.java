package com.example.stat.service;

import com.example.stat.util.HashUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DataUpdateService {
    private static final Logger logger = LogManager
            .getLogger(DataUpdateService.class);

    private MongoTemplate mongoTemplate;
    private CacheManager cacheManager;
    private static final String OK = "Update database ok. Invalidate cache ok";

    @Scheduled(fixedDelay = 10000) // 300000 ms = 5 minutes
    public void updateDataFromJsonFile() {
        try(InputStream inputStream = new ClassPathResource("test_report.json").getInputStream()) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            String newHash = HashUtil.calculateHash(json);
            Query query = new Query(Criteria.where("id")
                    .is("reportHash"));

            Document currentHashDocument = mongoTemplate
                    .findOne(query, Document.class, "metadata");

            String currentHash = Optional.ofNullable(currentHashDocument)
                    .map(doc -> doc.getString("hash"))
                    .orElse(null);

                logger.info("current hash ok");
                if (!newHash.equals(currentHash) || !mongoTemplate.collectionExists("report")) {
                    updateDatabase(json, newHash);
                    logger.warn(OK);
                }

        } catch (Exception e) {
            logger.error("Error updating data from file: {} | {}", e.getMessage(),  e);
        }
    }

    public void updateDatabase(String json, String newHash) {
        Document newReport = Document.parse(json);
        mongoTemplate.dropCollection("report");
        mongoTemplate.save(newReport, "report");

        Query query = new Query(Criteria.where("id").is("reportHash"));
        Update update = new Update().set("hash", newHash);
        mongoTemplate.upsert(query, update, "metadata");
        invalidateCache();
    }

    private void invalidateCache() {
        // All associated caches are invalidated
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects
                        .requireNonNull(cacheManager.getCache(cacheName))
                        .clear());
    }

}
