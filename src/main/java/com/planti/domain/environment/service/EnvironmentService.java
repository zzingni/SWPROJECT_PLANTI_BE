package com.planti.domain.environment.service;

import com.planti.domain.environment.dto.request.EnvironmentCreateRequest;
import com.planti.domain.environment.dto.response.EnvironmentResponse;
import com.planti.domain.environment.entity.Environment;
import com.planti.domain.environment.repository.EnvironmentRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;

    public EnvironmentResponse create(EnvironmentCreateRequest req) {

        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("환경 사용자 없음. user_id=1")); // user_id 고정

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
}