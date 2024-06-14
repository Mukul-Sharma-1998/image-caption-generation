package com.example.image_caption_generator.service;


import com.example.image_caption_generator.dto.geminiRequest.Content;
import com.example.image_caption_generator.dto.geminiRequest.ContentRequest;
import com.example.image_caption_generator.dto.geminiRequest.InlineData;
import com.example.image_caption_generator.dto.geminiRequest.Part;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Collections;
import java.util.List;

@Service
public class GoogleGenerativeLanguageService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${API_KEY}")
    private String API_KEY;

    @Value("${API_URL}")
    private String API_URL;

    public String generateCaption(String base64EncodedImage, String text) {
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
                String responseBody = response.getBody();
                String extractedText = extractTextFromResponse(responseBody);
                return extractedText;

            } else {
                throw new RuntimeException("Failed to call API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating request JSON", e);
        }
    }


    private String extractTextFromResponse(String responseString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Parse the response string into a JsonNode
            JsonNode rootNode = mapper.readTree(responseString);

            // Get the "candidates" array
            JsonNode candidatesArray = rootNode.get("candidates");

            // Get the first candidate (assuming there's only one candidate in the array)
            JsonNode firstCandidate = candidatesArray.get(0);

            // Get the "content" object
            JsonNode contentObject = firstCandidate.get("content");

            // Get the "parts" array
            JsonNode partsArray = contentObject.get("parts");

            // Initialize a StringBuilder to store the extracted text
            StringBuilder extractedText = new StringBuilder();

            // Iterate through the "parts" array and append the text to the StringBuilder
            for (JsonNode partNode : partsArray) {
                String text = partNode.get("text").asText();
                text = text.replaceAll("```json", "");
                text = text.replaceAll("```", "");
                text = text.trim();
                extractedText.append(text).append("\n");
            }

            // Extracted text as a string
            return extractedText.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error extracting text from response", e);
        }
    }

//    private String formatResponse(GeminiResponse apiResponse) {
//        // Format the response as per your requirements
//        StringBuilder formattedResponse = new StringBuilder();
//        formattedResponse.append("Captions:\n");
//        for (String caption : apiResponse.getCaptions()) {
//            formattedResponse.append("- ").append(caption).append("\n");
//        }
//        formattedResponse.append("\nHashtags:\n");
//        for (String hashtag : apiResponse.getHashtags()) {
//            formattedResponse.append("#").append(hashtag).append(" ");
//        }
//        return formattedResponse.toString();
//    }
}
