package com.example.library.repository.mongo;

import com.example.library.entity.mongo.Id39586BookDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Id39586BookMongoRepository extends MongoRepository<Id39586BookDocument, String> {
}