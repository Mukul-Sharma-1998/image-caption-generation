package com.example.image_caption_generator.service;

import com.example.image_caption_generator.entity.User;
import com.example.image_caption_generator.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserEntityRepository userEntityRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userEntityRepository.findByEmail(email);
    }

    @Override
    public void save(User user) {
        userEntityRepository.save(user);
    }
}
