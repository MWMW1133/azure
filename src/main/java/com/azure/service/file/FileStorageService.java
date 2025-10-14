package com.azure.service.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 단순 로컬 파일 저장 서비스 (ex: /uploads 디렉토리)
 */
@Service
public class FileStorageService {

    private final Path rootLocation = Path.of("uploads"); // 저장 폴더

    public String save(MultipartFile file) throws IOException {
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path destination = rootLocation.resolve(fileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString(); // storageKey로 반환
    }

    /** 파일 삭제 */
    public void delete(String storageKey) throws IOException {
        if (storageKey == null || storageKey.isBlank()) return;

        Path path = Path.of(storageKey);
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("[DEBUG] 파일 삭제 완료: " + path);
        } else {
            System.out.println("[DEBUG] 삭제할 파일이 존재하지 않음: " + path);
        }
    }
}
