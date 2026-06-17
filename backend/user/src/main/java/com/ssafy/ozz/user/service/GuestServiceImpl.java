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
    public long createGuest() {
        String guestEmail = "g"
                + Long.toString(System.currentTimeMillis(), 36)
                + Long.toString(System.nanoTime(), 36)
                + "@o.zz";

        User guestUser = userRepository.save(User.builder()
                .birth(new Date())
                .email(guestEmail)
                .name("guest")
                .isGuest(true)
                .build());
        guestUser.updateNickname("guest" + guestUser.getId());
        return guestUser.getId();
    }
}
