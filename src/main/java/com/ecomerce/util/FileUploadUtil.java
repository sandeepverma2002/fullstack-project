package com.ecomerce.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUploadUtil {

    public static String saveFile(MultipartFile file) throws IOException {
        // Check if the file is null or empty
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or not provided");
        }

        // Define the upload directory
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        // Ensure the directory exists
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Generate a unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File uploadFile = new File(uploadDir + fileName);

        // Log the upload process
        System.out.println("Uploading file to: " + uploadFile.getAbsolutePath());

        // Transfer the file to the server
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }

        return fileName; // Return the filename for storage in the database
    }
}