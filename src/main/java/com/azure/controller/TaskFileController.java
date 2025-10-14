package com.azure.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azure.dto.FileObjectDTO;
import com.azure.model.file.FileObject;
import com.azure.model.user.User;
import com.azure.repository.FileObjectRepository;
import com.azure.service.file.FileStorageService;
import com.azure.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/files")
@RequiredArgsConstructor
public class TaskFileController {

    private final FileObjectRepository fileRepo;
    private final TaskService taskService;
    private final FileStorageService storageService; // S3나 local file service

    @PostMapping("/upload")
    public ResponseEntity<FileObjectDTO> uploadFile(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @ModelAttribute("currentUserId") Long uid
    ) throws IOException {
        // 1️⃣ 파일 저장
        String key = storageService.save(file);

        // 2️⃣ FileObject 생성
        FileObject f = new FileObject();
        f.setFileName(file.getOriginalFilename());
        f.setMimeType(file.getContentType());
        f.setSize(file.getSize());
        f.setStorageKey(key);
        User uploader = new User();   // 기본 생성자
        uploader.setId(uid);   // ID만 세팅해서 참조
        f.setUploader(uploader);
        fileRepo.save(f);

        // 3️⃣ Task 연결
        taskService.addAttachment(taskId, f.getId());

        // 4️⃣ DTO 반환
        FileObjectDTO dto = new FileObjectDTO();
        dto.setId(f.getId());
        dto.setFileName(f.getFileName());
        dto.setMimeType(f.getMimeType());
        dto.setSize(f.getSize());
        dto.setCreatedAt(f.getCreatedAt());
        return ResponseEntity.ok(dto);
    }
}

