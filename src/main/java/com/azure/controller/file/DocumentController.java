package com.azure.controller.file;

import com.azure.model.user.User;
import com.azure.service.ProjectService;
import com.azure.service.file.DocumentService;
import com.azure.service.file.FileService;
import com.azure.service.file.FileStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final FileService fileService;
    private final ProjectService projectService;

    /**
     * 📂 파일 목록 불러오기 (JSP fragment)
     */
    @GetMapping
    public String listDocuments(@PathVariable Long projectId,
                                Pageable pageable,
                                Model model,
                                HttpSession session) {
        System.out.println("[DEBUG] listDocuments 호출됨 projectId=" + projectId);

        var page = documentService.listByProject(projectId, pageable);
        model.addAttribute("documents", page.getContent());
        model.addAttribute("projectId", projectId);

        //  세션에서 로그인 유저 수동 주입
        var loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null) {
            System.out.println("[DEBUG] loginUser.id=" + loginUser.getId());
            model.addAttribute("user", loginUser);
        } else {
            System.out.println("[DEBUG] 세션에 loginUser 없음 (null)");
        }

        return "projects/fragments/files"; // JSP fragment 경로
    }

    /**
     *  파일 업로드
     */
    @PostMapping("/upload")
    public String upload(@PathVariable Long projectId,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam("authorId") Long authorId,
                         Model model,
                         Pageable pageable,
                         HttpSession session) throws IOException {
        System.out.println("[DEBUG] upload 요청 projectId=" + projectId + ", authorId=" + authorId);

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getOrganization() == null) {
            throw new IllegalStateException("로그인 사용자나 조직 정보를 찾을 수 없습니다.");
        }

        try {
            // 파일 업로드 (IOException 처리)
            Long orgId = loginUser.getOrganization().getId();
            fileService.upload(orgId, authorId, file);

            // 업로드 후 목록 리로드
            var page = documentService.listByProject(projectId, pageable);
            model.addAttribute("documents", page.getContent());
            model.addAttribute("projectId", projectId);
            model.addAttribute("user", loginUser); // 중복 변수 제거, 바로 사용

            return "projects/fragments/files";

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("projectId", projectId);
            model.addAttribute("user", loginUser);
            return "projects/fragments/files";
        }
    }
}
