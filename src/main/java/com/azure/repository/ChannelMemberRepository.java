package com.azure.repository;

import com.azure.model.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, com.azure.model.ChannelMemberId> {}
