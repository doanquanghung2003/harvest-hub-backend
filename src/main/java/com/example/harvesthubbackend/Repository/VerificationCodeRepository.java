package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.VerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmailAndCode(String email, String code);
    Optional<VerificationCode> findByEmail(String email);
    List<VerificationCode> findAllByEmail(String email);
    void deleteByEmail(String email);
}

