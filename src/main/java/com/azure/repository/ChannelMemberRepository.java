package com.azure.repository;

import com.azure.model.ChannelMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMemberRepository extends JpaRepository<ChannelMemberEntity, Long> {
}
