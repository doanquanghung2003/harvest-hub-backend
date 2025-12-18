package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.VerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmailAndCode(String email, String code);
    Optional<VerificationCode> findByEmail(String email);
    void deleteByEmail(String email);
}

