package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.tag.Tag;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByProjectId(Long projectId);
}
