package com.example.image_caption_generator.service;


import com.example.image_caption_generator.dto.geminiRequest.Content;
import com.example.image_caption_generator.dto.geminiRequest.ContentRequest;
import com.example.image_caption_generator.dto.geminiRequest.InlineData;
import com.example.image_caption_generator.dto.geminiRequest.Part;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Collections;
import java.util.List;

@Service
public class GoogleGenerativeLanguageService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={apiKey}";

    public String generateCaption(String API_KEY, String base64EncodedImage, String text) {
        // Create the request JSON structure
        ContentRequest contentRequest = new ContentRequest();
        Content content = new Content();
        Part textPart = new Part();
        textPart.setText(text);

        InlineData inlineData = new InlineData();
        inlineData.setMimeType("image/jpeg");
        inlineData.setData(base64EncodedImage);

        Part imagePart = new Part();
        imagePart.setInlineData(inlineData);

        content.setParts(List.of(textPart, imagePart));
        contentRequest.setContents(Collections.singletonList(content));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestJson = objectMapper.writeValueAsString(contentRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class, API_KEY);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to call API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating request JSON", e);
        }
    }
}
