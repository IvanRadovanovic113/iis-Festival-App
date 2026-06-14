package com.festivalapp.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootPath;

    public FileStorageService(@Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile saveAdContent(Long adId, MultipartFile file) {
        try {
            Files.createDirectories(rootPath.resolve("ads").resolve(String.valueOf(adId)));
            String sanitizedName = sanitizeFileName(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + "-" + sanitizedName;
            Path relativePath = Paths.get("ads", String.valueOf(adId), storedFileName);
            Path targetPath = rootPath.resolve(relativePath).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(
                relativePath.toString().replace('\\', '/'),
                sanitizedName,
                file.getContentType(),
                file.getSize()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store uploaded file", ex);
        }
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = rootPath.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Stored file is not available");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Stored file URL is invalid", ex);
        }
    }

    private String sanitizeFileName(String originalFileName) {
        String raw = originalFileName == null || originalFileName.isBlank() ? "content.bin" : originalFileName;
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalized.isBlank() ? "content.bin" : normalized;
    }

    @Getter
    public static class StoredFile {
        private final String storagePath;
        private final String originalFileName;
        private final String mimeType;
        private final long size;

        public StoredFile(String storagePath, String originalFileName, String mimeType, long size) {
            this.storagePath = storagePath;
            this.originalFileName = originalFileName;
            this.mimeType = mimeType;
            this.size = size;
        }
    }
}
