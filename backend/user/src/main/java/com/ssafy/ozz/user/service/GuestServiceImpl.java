package com.ssafy.ozz.user.service;

import com.ssafy.ozz.user.domain.User;
import com.ssafy.ozz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {
    private static final String DEMO_EMAIL = "demo@ozz.local";
    private static final String DEMO_NAME = "OZZ Demo";
    private static final String DEMO_NICKNAME = "ozz-demo";

    private final UserRepository userRepository;

    @Transactional
    @Override
    public long createGuest() {
        return userRepository.findByEmail(DEMO_EMAIL)
                .map(User::getId)
                .orElseGet(this::createDemoUser);
    }

    private long createDemoUser() {
        User demoUser = userRepository.save(User.builder()
                .birth(new Date())
                .email(DEMO_EMAIL)
                .name(DEMO_NAME)
                .nickname(DEMO_NICKNAME)
                .isGuest(true)
                .build());

        return demoUser.getId();
    }
}
