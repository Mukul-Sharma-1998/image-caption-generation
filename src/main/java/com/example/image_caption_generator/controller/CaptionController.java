package com.example.image_caption_generator.controller;


import com.example.image_caption_generator.service.GoogleGenerativeLanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@RestController
public class CaptionController {

    @Autowired
    private GoogleGenerativeLanguageService googleGenerativeLanguageService;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file) {
        System.out.println("Request reached!!");
        if(file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Please select a file to upload");
        }
        try{
            if(file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeded the maximum size limit of 100MB");
            }
            byte[] bytes = file.getBytes();
            System.out.println("file uploaded successfully!");
            String base64EncodedImage = Base64.getEncoder().encodeToString(bytes);
            if(!base64EncodedImage.isEmpty()) {
                System.out.println("Image encoded to Base64 successfully!");
                String captions = googleGenerativeLanguageService
                        .generateCaption("asdfgh", base64EncodedImage, "This is a picture that I want to upload on instagram. Can you suggest some funny caption ideas along with trending hashtags?");

                // Delete the uploaded file after processing
                File tempFile = File.createTempFile("uploaded-file-", ".tmp");
                file.transferTo(tempFile);
                if (!tempFile.delete()) {
                    System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
                }
                System.out.println("File is deleted successfully!");
                return ResponseEntity.ok().body(captions);
            } else {

                System.out.println("Failed to encode image to Base64!");
                return ResponseEntity.badRequest().body("Failed to encode your file!");
            }


        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the file: " + e.getMessage());
        }
    }

}
