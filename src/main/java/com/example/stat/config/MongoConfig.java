package com.example.stat.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${myapp.mongodb.username}")
    private String username;

    @Value("${myapp.mongodb.password}")
    private String password;

    @Value("${myapp.mongodb.database}")
    private String database;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients
                .create(String.format("mongodb://%s:%s@localhost:27017/",
                        username, password));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), database);
    }

}
