package com.example.library.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class Id39586MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    public Id39586MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        MongoCollection<Document> collection = mongoTemplate.getCollection("error_logs");

        collection.createIndex(Indexes.descending("timestamp"));
        collection.createIndex(Indexes.ascending("errorType", "timestamp"));
        collection.createIndex(Indexes.ascending("status", "timestamp"));
        collection.createIndex(Indexes.ascending("clientIp"));
        collection.createIndex(Indexes.ascending("userId"));

        IndexOptions ttlOptions = new IndexOptions()
                .expireAfter(2592000L, java.util.concurrent.TimeUnit.SECONDS); // 30 дней
        collection.createIndex(Indexes.ascending("timestamp"), ttlOptions);

        System.out.println("MongoDB indexes created/verified successfully");
    }
}