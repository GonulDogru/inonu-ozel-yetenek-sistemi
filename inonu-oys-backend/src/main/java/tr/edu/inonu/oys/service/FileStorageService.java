package tr.edu.inonu.oys.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(".pdf", ".png", ".jpg", ".jpeg");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");
    private static final Map<String, Set<String>> CONTENT_TYPES = Map.of(
            ".pdf", Set.of("application/pdf"),
            ".png", Set.of("image/png"),
            ".jpg", Set.of("image/jpeg", "image/jpg"),
            ".jpeg", Set.of("image/jpeg", "image/jpg")
    );

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Dosya yükleme dizini oluşturulamadı.", ex);
        }
    }

    public void validateDocument(MultipartFile file, String label, boolean imageOnly) {
        if (file == null || file.isEmpty()) throw new RuntimeException(label + " zorunludur.");
        if (file.getSize() > MAX_FILE_SIZE) throw new RuntimeException(label + " en fazla 10 MB olabilir.");

        String filename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (filename.isBlank() || filename.contains("..")) throw new RuntimeException(label + " dosya adı geçersiz.");
        String extension = extensionOf(filename);
        Set<String> allowed = imageOnly ? IMAGE_EXTENSIONS : DOCUMENT_EXTENSIONS;
        if (!allowed.contains(extension)) throw new RuntimeException(label + " dosya türü desteklenmiyor.");

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPES.get(extension).contains(contentType)) {
            throw new RuntimeException(label + " içerik türü dosya uzantısıyla uyuşmuyor.");
        }
        if (!hasExpectedSignature(file, extension)) {
            throw new RuntimeException(label + " içeriği geçerli bir " + extension.substring(1).toUpperCase(Locale.ROOT) + " dosyası değil.");
        }
    }

    public String storeFile(MultipartFile file, String username, String fileType) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String newName = username + "_" + fileType + "_" + UUID.randomUUID() + extensionOf(originalName);
        Path target = fileStorageLocation.resolve(newName).normalize();
        if (!target.startsWith(fileStorageLocation)) throw new RuntimeException("Geçersiz hedef dosya yolu.");
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return newName;
        } catch (IOException ex) {
            throw new RuntimeException("Dosya kaydedilemedi: " + originalName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(fileStorageLocation)) throw new RuntimeException("Geçersiz dosya yolu.");
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new RuntimeException("Dosya bulunamadı: " + fileName);
            return resource;
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Dosya bulunamadı: " + fileName, ex);
        }
    }

    private boolean hasExpectedSignature(MultipartFile file, String extension) {
        try (var input = file.getInputStream()) {
            byte[] header = input.readNBytes(8);
            return switch (extension) {
                case ".pdf" -> startsWith(header, new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});
                case ".png" -> startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
                case ".jpg", ".jpeg" -> startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
                default -> false;
            };
        } catch (IOException e) {
            throw new RuntimeException("Dosya içeriği okunamadı.", e);
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (value[i] != prefix[i]) return false;
        return true;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
