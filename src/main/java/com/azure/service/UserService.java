package com.azure.service;

import com.azure.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 관련 도메인 기능을 제공한다.
 * <ul>
 *   <li>컨트롤러는 레포지토리를 직접 사용하지 않고 본 서비스를 통해 접근한다.</li>
 *   <li>비밀번호는 해시된 값만 입력받도록 한다(해싱은 컨트롤러/필터에서 처리).</li>
 * </ul>
 */
public interface UserService {
    /** ID로 사용자 한 명을 조회한다. 존재하지 않으면 NotFoundException. */
    User get(Long id);

    /** 전체 사용자 목록(페이징). 관리자 화면 등에서 사용. */
    Page<User> list(Pageable pageable);

    /** 조직별 사용자 목록. 프로젝트 멤버 선택 등에 사용. */
    List<User> listByOrganization(Long organizationId);

    /**
     * 사용자를 생성한다.
     * @param organizationId organizations.id FK
     * @param loginId 사용자가 직접 입력한 로그인 ID (중복 불가)
     * @param passwordHash 해시된 비밀번호(예: BCrypt)
     * @param name 표시 이름
     * @param avatarUrl 아바타 URL(선택)
     * @param workStatus 근무 상태 (기본값: WORKING)
     */
    User create(Long organizationId, String loginId, String passwordHash,
                String name, String avatarUrl, User.WorkStatus workStatus);

    /** 프로필 필드 수정(조직/로그인ID 변경은 포함하지 않음). */
    User update(String passwordHash, String name, String avatarUrl, User.WorkStatus workStatus);

    /** 물리 삭제. 소프트 삭제가 필요하면 workStatus = LEAVE 등으로 대체 가능. */
    void delete(Long userId);

    /** 로그인 ID로 사용자 조회(없으면 Optional.empty()). */
    Optional<User> findByLoginId(String loginId);

    /** 로그인 ID 중복 여부 확인 */
    boolean existsByLoginId(String loginId);
}