package com.azure.controller.file;

import com.azure.model.user.User;
import com.azure.service.file.DocumentService;
import com.azure.service.file.FileServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.azure.model.file.FileObject;
import com.azure.model.document.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileServiceImpl fileServiceImpl;

    // 📂 파일 목록 불러오기 (JSP fragment)
    @GetMapping
    public String listDocuments(@PathVariable Long projectId,
                                Pageable pageable,
                                Model model,
                                HttpSession session) {
        System.out.println("[DEBUG] listDocuments 호출됨 projectId=" + projectId);

        var page = documentService.listByProject(projectId, pageable);
        // 회의록(webm 등) 제외
        List<Document> documents = page.getContent().stream()
                .filter(d -> d.getTitle() == null || !d.getTitle().toLowerCase().contains(".webm"))
                .toList();

        model.addAttribute("documents", documents);
        model.addAttribute("projectId", projectId);

        // 세션에서 로그인 유저 수동 주입
        var loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null) {
            System.out.println("[DEBUG] loginUser.id=" + loginUser.getId());
            model.addAttribute("user", loginUser);
        } else {
            System.out.println("[DEBUG] 세션에 loginUser 없음 (null)");
        }

        return "projects/fragments/files";
    }

    // 파일 업로드
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
            Long orgId = loginUser.getOrganization().getId();

            // 파일 저장 (file_objects insert)
            FileObject fileObject = fileServiceImpl.upload(orgId, authorId, file);

            // 문서 생성 (documents insert)
            var document = documentService.create(projectId, authorId, file.getOriginalFilename(), null);

            // 문서 버전 추가 (document_versions insert)
            documentService.addVersion(document.getId(), fileObject.getId(), authorId, null);

            System.out.printf("[DEBUG] ✅ 업로드 성공: fileId=%d, documentId=%d, authorId=%d%n",
                    fileObject.getId(), document.getId(), authorId);

            // 업로드 후 목록 리로드
            var page = documentService.listByProject(projectId, pageable);
            model.addAttribute("documents", page.getContent());
            model.addAttribute("projectId", projectId);
            model.addAttribute("user", loginUser);

            return "projects/fragments/files";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("projectId", projectId);
            model.addAttribute("user", loginUser);
            return "projects/fragments/files";
        }
    }

    // 템플릿 로딩
    @GetMapping("/new")
    public String newDocument(@PathVariable Long projectId,
                              @RequestParam(defaultValue = "meeting") String templateKey,
                              Model model) throws IOException {
        Path path = Paths.get("src/main/resources/templates/document-templates/" + templateKey + ".html");
        String templateHtml = Files.readString(path);

        model.addAttribute("templateHtml", templateHtml);
        model.addAttribute("projectId", projectId);
        return "editor/ckeditor";
    }

    // HTML 저장
    @PostMapping("/save")
    public String saveDocument(@PathVariable Long projectId,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content,
                               Pageable pageable,
                               Model model,
                               HttpSession session) throws IOException {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getOrganization() == null) {
            throw new IllegalStateException("로그인 사용자나 조직 정보를 찾을 수 없습니다.");
        }

        Long orgId = loginUser.getOrganization().getId();
        Long authorId = loginUser.getId();

        var tempFile = Files.createTempFile("meeting-", ".html");
        Files.writeString(tempFile, content);
        var multipartFile = new org.springframework.mock.web.MockMultipartFile(
                tempFile.getFileName().toString(), title + ".html", "text/html", Files.readAllBytes(tempFile)
        );

        FileObject fileObject = fileServiceImpl.upload(orgId, authorId, multipartFile);
        var document = documentService.create(projectId, authorId, title, null);
        documentService.addVersion(document.getId(), fileObject.getId(), authorId, null);

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/save/pdf")
    public String saveDocumentAsPdf(@PathVariable Long projectId,
                                    @RequestParam("title") String title,
                                    @RequestParam("content") String content,
                                    Pageable pageable,
                                    Model model,
                                    HttpSession session) throws IOException {
        return saveWithFormat(projectId, title, content, "pdf", pageable, model, session);
    }

    @PostMapping("/save/docx")
    public String saveDocumentAsDocx(@PathVariable Long projectId,
                                     @RequestParam("title") String title,
                                     @RequestParam("content") String content,
                                     Pageable pageable,
                                     Model model,
                                     HttpSession session) throws IOException {
        return saveWithFormat(projectId, title, content, "docx", pageable, model, session);
    }

    // 공통 저장 로직 (PDF/DOCX)
    private String saveWithFormat(Long projectId,
                                  String title,
                                  String content,
                                  String format,
                                  Pageable pageable,
                                  Model model,
                                  HttpSession session) throws IOException {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getOrganization() == null) {
            throw new IllegalStateException("로그인 사용자나 조직 정보를 찾을 수 없습니다.");
        }

        try {
            Long orgId = loginUser.getOrganization().getId();
            Long authorId = loginUser.getId();
            byte[] fileBytes;
            String mimeType;
            String extension;

            // 회의록(webm)은 변환하지 않음
            if (title.toLowerCase().endsWith(".webm")) {
                System.out.println("[WARN] 회의록(webm) 파일은 문서 저장 대상 아님: " + title);
                return "redirect:/projects/" + projectId;
            }

            if ("pdf".equalsIgnoreCase(format)) {
                // HTML → PDF 변환
                ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
                com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder =
                        new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
                builder.usePdfAConformance(
                        com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance.PDFA_1_B
                );
                builder.useFastMode();

                File malgunFont = new File("C:/Windows/Fonts/malgun.ttf");
                if (malgunFont.exists()) {
                    builder.useFont(malgunFont, "Malgun Gothic");
                }

                // 본문 정규화
                String sanitized = content
                        .replaceAll("(?is)<!DOCTYPE.*?>", "")
                        .replaceAll("(?is)</?html[^>]*>", "")
                        .replaceAll("(?is)</?head[^>]*>", "")
                        .replaceAll("(?is)<meta\\b[^>]*>", "")
                        .replaceAll("(?is)<style\\b[^>]*>.*?</style>", "")
                        .replaceAll("(?i)<br>", "<br />")
                        .replaceAll("(?i)<hr>", "<hr />")
                        .replaceAll("(?i)<img([^>/]*?)>", "<img$1 />");

                String html = """
                    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                    <html xmlns="http://www.w3.org/1999/xhtml" lang="ko">
                    <head>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                        <style type="text/css">
                            @page { size: A4; margin: 20mm; }
                            body { font-family: 'Malgun Gothic','나눔고딕',sans-serif; }
                        </style>
                    </head>
                    <body>
                    """ + sanitized + """
                    </body>
                    </html>
                    """;

                builder.withHtmlContent(html, null);
                builder.toStream(pdfOutput);
                builder.run();

                fileBytes = pdfOutput.toByteArray();
                mimeType = "application/pdf";
                extension = ".pdf";

            } else if ("docx".equalsIgnoreCase(format)) {
                // HTML → DOCX 변환 (간단 버전)
                org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument();
                org.apache.poi.xwpf.usermodel.XWPFParagraph p = doc.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun run = p.createRun();

                // HTML 본문 정리
                String pureText = content
                        .replaceAll("(?is)<style[^>]*>.*?</style>", "")   // <style> 제거
                        .replaceAll("(?is)<script[^>]*>.*?</script>", "") // <script> 제거
                        .replaceAll("(?i)<br\\s*/?>", "\n")                // <br> → 줄바꿈
                        .replaceAll("(?i)</p>", "\n\n")                    // </p> → 단락 줄바꿈
                        .replaceAll("(?i)<[^>]*>", "")                     // 나머지 HTML 태그 제거
                        .replaceAll("&nbsp;", " ")                         // 공백 정리
                        .replaceAll("\\s+", " ")                           // 중복 공백 제거
                        .trim();

                run.setText(pureText);
                run.setFontFamily("Malgun Gothic"); // 한글 폰트 지정
                run.setFontSize(11);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                doc.write(out);

                fileBytes = out.toByteArray();
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                extension = ".docx";
//                run.setText(content.replaceAll("<[^>]*>", "")); // HTML 태그 제거
//
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                doc.write(out);
//
//                fileBytes = out.toByteArray();
//                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
//                extension = ".docx";

            } else {
                throw new IllegalArgumentException("지원되지 않는 파일 형식입니다: " + format);
            }

            var multipartFile = new org.springframework.mock.web.MockMultipartFile(
                    title + extension,
                    title + extension,
                    mimeType,
                    fileBytes
            );

            // 업로드 + 문서/버전 생성
            FileObject fileObject = fileServiceImpl.upload(orgId, authorId, multipartFile);
            var document = documentService.create(projectId, authorId, title, null);
            documentService.addVersion(document.getId(), fileObject.getId(), authorId, null);

            System.out.printf("[DEBUG] ✅ %s 저장 성공: fileId=%d, documentId=%d, authorId=%d%n",
                    format.toUpperCase(), fileObject.getId(), document.getId(), authorId);

            return "redirect:/projects/" + projectId;

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "문서 저장 중 오류 발생: " + e.getMessage());
            model.addAttribute("projectId", projectId);
            // ⚠️ 여기서 loginUser 재선언하지 않고, 위에서 만든 변수 그대로 사용
           
            model.addAttribute("user", loginUser);
            return "redirect:/projects/" + projectId;
        }
    }
}
