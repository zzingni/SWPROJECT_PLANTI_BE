package com.planti.domain.user.service;

import com.planti.domain.user.repository.LoginUserRepository;
import com.planti.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.image.BandCombineOp;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginUserRepository loginUserRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

}
