package com.planti.domain.user.repository;

import com.planti.domain.user.entity.User;
import com.planti.domain.userplant.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.userId from User u where u.loginId = :loginId")
    Optional<Long> findIdByLoginId(@Param("loginId") String loginId);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 로그인 ID로 사용자 ID 조회 메서드
    Optional<User> findByLoginId(String loginId);

    // nickname 중복 체크
    boolean existsByNickname(String nickname);
    // 특정 로그인 ID가 이미 데이터베이스에 존재하는지 여부를 확인하는 메서드
    boolean existsByLoginId(String loginId);
}