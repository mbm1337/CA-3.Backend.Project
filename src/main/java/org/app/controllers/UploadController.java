package org.app.controllers;

import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class UploadController {
    public static Handler uploadImage = ctx -> {
        UploadedFile file = ctx.uploadedFile("image");
        if (file != null) {
            String fileName = UUID.randomUUID().toString() + "_" + file.filename();
            Path filePath = Paths.get("src/main/resources/public/images", fileName);
            Files.copy(file.content(), filePath);
            ctx.status(200).json(Map.of("message", "File uploaded successfully", "fileName", fileName));
        } else {
            ctx.status(400).result("No file uploaded");
        }
    };
}
