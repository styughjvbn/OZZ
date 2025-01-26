package com.ssafy.ozz.user.service;

import com.ssafy.ozz.user.domain.User;
import com.ssafy.ozz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService{
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User createGuest() {
        User guestUser = userRepository.save(User.builder()
                .birth(new Date())
                .email("guest@guest.com")
                .name("guest")
                .isGuest(true)
                .build());
        guestUser.updateNickname("guest"+guestUser.getId());
        return guestUser;
    }
}
