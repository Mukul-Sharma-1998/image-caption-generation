package com.example.image_caption_generator.service;

public interface GenerativeLanguageService {

    public String generateCaption(String base64EncodedImage, String text);
}
