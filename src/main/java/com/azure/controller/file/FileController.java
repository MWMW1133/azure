package com.azure.controller.file;


import com.azure.model.file.FileObject;
import com.azure.model.user.User;
import com.azure.service.file.FileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    /** 파일 저장소 페이지 (회사 단위 전체 파일 목록) */
    @GetMapping
    public String fileStorage(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        Long orgId = loginUser.getOrganization().getId();
        List<FileObject> files = fileService.listByOrganization(orgId);

        model.addAttribute("activePage", "files");
        model.addAttribute("body", "workspace/file-storage.jsp");
        model.addAttribute("files", files);

        return "mainbar";
    }

    /**  파일 업로드 (AJAX or Form POST) */
    @PostMapping("/upload")
    @ResponseBody
    public FileObject uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Long orgId = loginUser.getOrganization().getId();
        return fileService.upload(orgId, loginUser.getId(), file);
    }

    /**  파일 삭제 */
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteFile(@PathVariable Long id, HttpSession session) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        fileService.delete(id);
        return "OK";
    }

    /** (선택) 검색 + 페이징 API */
    @GetMapping("/search")
    @ResponseBody
    public Object search(@RequestParam String q,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        Long orgId = loginUser.getOrganization().getId();
        return fileService.searchByName(orgId, q, PageRequest.of(page, size));
    }

    // 미리보기
    @GetMapping("/{id}/view")
    public void viewFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FileObject file = fileService.get(id);
        response.setContentType(file.getMimeType());
        Files.copy(Paths.get(file.getStorageKey()), response.getOutputStream());
    }

    // 다운로드
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FileObject file = fileService.get(id);
        response.setContentType(file.getMimeType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8) + "\"");
        Files.copy(Paths.get(file.getStorageKey()), response.getOutputStream());
    }
}