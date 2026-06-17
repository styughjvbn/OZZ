package com.ssafy.ozz.user.global.config;

import com.ssafy.ozz.user.service.DemoAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoAccountInitializer implements ApplicationRunner {
    private final DemoAccountService demoAccountService;

    @Override
    public void run(ApplicationArguments args) {
        demoAccountService.getOrCreateDemoUserId();
    }
}
