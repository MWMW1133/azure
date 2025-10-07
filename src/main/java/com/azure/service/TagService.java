package com.azure.service;

import java.util.List;

import com.azure.model.tag.Tag;

public interface TagService {
    /** 프로젝트의 태그 목록 */
    List<Tag> listByProject(Long projectId);

    /** 태그 추가 (중복/유효성 검사 포함) */
    Tag create(Long projectId, String name);

    /** 태그 삭제 (프로젝트 소속 검증 포함) */
    void delete(Long projectId, Long tagId);
}
