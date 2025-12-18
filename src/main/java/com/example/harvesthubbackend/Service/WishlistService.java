package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Wishlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

// Repository
@Repository
interface WishlistRepository extends MongoRepository<Wishlist, String> {
    List<Wishlist> findByUserId(String userId);
}

// Service
@Service
public class WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;

    public List<Wishlist> getAll() {
        return wishlistRepository.findAll();
    }

    public Wishlist getById(String id) {
        return wishlistRepository.findById(id).orElse(null);
    }

    public List<Wishlist> getByUserId(String userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public Wishlist create(Wishlist wishlist) {
        return wishlistRepository.save(wishlist);
    }

    public Wishlist update(String id, Wishlist wishlist) {
        wishlist.setId(id);
        return wishlistRepository.save(wishlist);
    }

    public void delete(String id) {
        wishlistRepository.deleteById(id);
    }
}


