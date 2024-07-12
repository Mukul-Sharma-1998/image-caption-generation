package com.example.image_caption_generator.repository;

import com.example.image_caption_generator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
