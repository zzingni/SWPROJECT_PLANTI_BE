package com.planti.domain.environment.service;

import com.planti.domain.environment.dto.request.EnvironmentCreateRequest;
import com.planti.domain.environment.dto.response.EnvironmentResponse;
import com.planti.domain.environment.entity.Environment;
import com.planti.domain.environment.repository.EnvironmentRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;

    private static final Long ENV_USER_ID = 27L;

    @Transactional
    public EnvironmentResponse create(EnvironmentCreateRequest req) {
        User user = userRepository.findById(ENV_USER_ID)
                .orElseThrow(() -> new IllegalStateException("환경 사용자 없음. user_id=" + ENV_USER_ID));

        Environment env = new Environment();
        env.setUser(user);
        env.setTemperature(req.temperature());
        env.setHumidity(req.humidity());

        Environment saved = environmentRepository.save(env);

        return new EnvironmentResponse(
                saved.getEnvironmentId(),
                saved.getTemperature(),
                saved.getHumidity(),
                saved.getRecordedAt()
        );
    }

    public EnvironmentResponse getLatest(Long userId) {
        Environment env = environmentRepository
                .findTopByUser_UserIdOrderByRecordedAtDesc(userId)
                .orElseThrow();

        return EnvironmentResponse.from(env);
    }

}