package com.ssafy.ozz.user.service;

import com.ssafy.ozz.user.domain.User;
import com.ssafy.ozz.user.global.config.DemoAccountProperties;
import com.ssafy.ozz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Service
@RequiredArgsConstructor
public class DemoAccountServiceImpl implements DemoAccountService {
    private final UserRepository userRepository;
    private final DemoAccountProperties demoAccountProperties;

    @Transactional
    @Override
    public long getOrCreateDemoUserId() {
        return userRepository.findByEmail(demoAccountProperties.getEmail())
                .map(User::getId)
                .orElseGet(this::createDemoUser);
    }

    private long createDemoUser() {
        User demoUser = userRepository.save(User.builder()
                .birth(Date.valueOf(demoAccountProperties.getBirth()))
                .email(demoAccountProperties.getEmail())
                .name(demoAccountProperties.getName())
                .nickname(demoAccountProperties.getNickname())
                .isGuest(true)
                .build());

        return demoUser.getId();
    }
}
