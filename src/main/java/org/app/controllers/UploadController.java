package org.app.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class UploadController {

    private static final String UPLOAD_DIR = "/api/images";

    public static Handler uploadImage = ctx -> {
        UploadedFile file = ctx.uploadedFile("image");
        if (file != null) {
            // Ensure the upload directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.filename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.content(), filePath);
            ctx.status(200).json(Map.of("message", "File uploaded successfully", "fileName", fileName));
        } else {
            ctx.status(400).result("No file uploaded");
        }
    };

    public static Handler sendFile = ctx -> {
        String fileName = ctx.pathParam("filename");
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        if (Files.exists(filePath)) {
            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                // Determine the content type and set it
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream"; // Default content type
                }
                ctx.contentType(contentType);
                ctx.result(fis);
            } catch (IOException e) {
                ctx.status(500).result("Error reading file");
            }
        } else {
            ctx.status(404).result("File not found");
        }
    };
}
