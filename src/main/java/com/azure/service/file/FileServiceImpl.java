    package com.azure.service.file;

    import com.azure.model.Organization;
    import com.azure.model.file.FileObject;
    import com.azure.model.user.User;
    import com.azure.repository.DocumentRepository;
    import com.azure.repository.DocumentVersionRepository;
    import com.azure.repository.FileObjectRepository;
    import com.azure.service.exception.NotFoundException;
    import jakarta.persistence.EntityManager;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class FileServiceImpl {

        private final FileObjectRepository fileObjectRepository;
        private final FileStorageService fileStorageService;
        private final EntityManager entityManager;
        private final DocumentVersionRepository documentVersionRepository;
        private final DocumentRepository documentRepository;

        /**  회사 단위 파일 목록 조회 */
        @Transactional(readOnly = true)
        public List<FileObject> listByOrganization(Long orgId) {
            return fileObjectRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId);
        }

        /** 회사 단위 전체 파일(페이징) */
        @Transactional(readOnly = true)
        public Page<FileObject> pageByOrganization(Long orgId, Pageable pageable) {
            return fileObjectRepository.findByOrganization_Id(orgId, pageable);
        }

        /** (선택) 파일명 검색 */
        @Transactional(readOnly = true)
        public Page<FileObject> searchByName(Long orgId, String q, Pageable pageable) {
            return fileObjectRepository.findByOrganization_IdAndFileNameContainingIgnoreCase(orgId, q, pageable);
        }

        /** 단건 조회 */
        @Transactional(readOnly = true)
        public FileObject get(Long id) {
            return fileObjectRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("File not found: " + id));
        }

        /** 🔹 파일 업로드 (물리 저장 + DB 기록) */
        @Transactional
        public FileObject upload(Long orgId, Long uploaderId, MultipartFile multipart) throws IOException {
            String storageKey = fileStorageService.save(multipart);  // /uploads 경로에 저장

            FileObject f = new FileObject();
            f.setOrganization(new Organization());
            f.getOrganization().setId(orgId);
            User uploaderRef = entityManager.getReference(User.class, uploaderId);
            f.setUploader(uploaderRef);
    //        f.setUploader(new User()); f.getUploader().setId(uploaderId);
            f.setStorageKey(storageKey);
            f.setFileName(multipart.getOriginalFilename());
            f.setMimeType(multipart.getContentType());
            f.setSize(multipart.getSize());
            f.setTask(null);
            return fileObjectRepository.save(f);
        }

        /** 🔹 파일 삭제 (DB + 물리 파일) */
        @Transactional
        public void delete(Long fileId) {
            // 1️⃣ fileId로 연결된 문서 ID들 찾기
            List<Long> docIds = documentVersionRepository.findDocumentIdsByFileId(fileId);

            for (Long documentId : docIds) {
                documentVersionRepository.deleteByDocument_Id(documentId);
                documentRepository.deleteById(documentId);
            }

            // 2️⃣ fileObject 삭제
            var fileOpt = fileObjectRepository.findById(fileId);
            if (fileOpt.isPresent()) {
                var file = fileOpt.get();
                try {
                    fileStorageService.delete(file.getStorageKey());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileObjectRepository.delete(file);
            }
        }

    }
