package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Wishlist;
import com.example.harvesthubbackend.Service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public List<Wishlist> getAll() { return wishlistService.getAll(); }

    @GetMapping("/{id}")
    public Wishlist getById(@PathVariable String id) { return wishlistService.getById(id); }

    @GetMapping("/user/{userId}")
    public List<Wishlist> getByUser(@PathVariable String userId) { return wishlistService.getByUserId(userId); }

    @PostMapping
    public Wishlist create(@RequestBody Wishlist wishlist) { return wishlistService.create(wishlist); }

    @PutMapping("/{id}")
    public Wishlist update(@PathVariable String id, @RequestBody Wishlist wishlist) { return wishlistService.update(id, wishlist); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { wishlistService.delete(id); }
}


