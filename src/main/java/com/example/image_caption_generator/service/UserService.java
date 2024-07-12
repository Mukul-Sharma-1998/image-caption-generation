package com.example.image_caption_generator.service;

import com.example.image_caption_generator.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    void save(User user);
}
