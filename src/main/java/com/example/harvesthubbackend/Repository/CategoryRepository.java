package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrue();

    List<Category> findByParentId(String parentId);

    List<Category> findByLevel(int level);
}
