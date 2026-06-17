package com.ssafy.ozz.user.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

@Getter
@Setter
@ConfigurationProperties(prefix = "demo.account")
public class DemoAccountProperties {
    private String email = "demo@ozz.local";
    private String name = "OZZ Demo";
    private String nickname = "ozz-demo";
    private LocalDate birth = LocalDate.of(2000, 1, 1);
}
