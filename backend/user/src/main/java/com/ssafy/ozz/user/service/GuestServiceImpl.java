package com.ssafy.ozz.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {
    private final DemoAccountService demoAccountService;

    @Transactional
    @Override
    public long createGuest() {
        return demoAccountService.getOrCreateDemoUserId();
    }
}
