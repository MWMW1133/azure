package com.azure.service.impl;

import com.azure.model.document.Document;
import com.azure.model.document.DocumentVersion;
import com.azure.model.file.FileObject;
import com.azure.model.project.Project;
import com.azure.model.user.User;
import com.azure.repository.DocumentRepository;
import com.azure.repository.DocumentVersionRepository;
import com.azure.repository.FileObjectRepository;
import com.azure.service.DocumentService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 문서/버전 서비스 구현.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final FileObjectRepository fileObjectRepository;

    @Override @Transactional(readOnly = true)
    public Document get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<Document> listByProject(Long projectId, Pageable pageable) {
        List<Document> all = documentRepository.findByProjectId(projectId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Document> content = (start > end) ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @Override
    public Document create(Long projectId, Long authorId, String title, String templateKey) {
        Document d = new Document();
        d.setProject(new Project()); d.getProject().setId(projectId);
        d.setAuthor(new User()); d.getAuthor().setId(authorId);
        d.setTitle(title); d.setTemplateKey(templateKey);
        return documentRepository.save(d);
    }

    @Override
    public Document update(Long documentId, String title, String templateKey) {
        Document d = get(documentId);
        if (title != null) d.setTitle(title);
        if (templateKey != null) d.setTemplateKey(templateKey);
        return documentRepository.save(d);
    }

    @Override
    public void delete(Long documentId) { documentRepository.deleteById(documentId); }

    @Override
    public DocumentVersion addVersion(Long documentId, Long fileId, Long authorId, Integer versionNum) {
        FileObject file = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        DocumentVersion v = new DocumentVersion();
        v.setDocument(get(documentId));
        v.setFile(file);
        v.setAuthor(new User()); v.getAuthor().setId(authorId);
        v.setVersionNum(versionNum);
        return documentVersionRepository.save(v);
    }
}
