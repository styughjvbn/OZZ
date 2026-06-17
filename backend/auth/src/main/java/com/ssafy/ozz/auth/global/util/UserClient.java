package com.ssafy.ozz.auth.global.util;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ozz-user", url = "${USER_SERVICE_URL:}", path = "/api/users")
public interface UserClient {

    @PostMapping("/signup/guest")
    ResponseEntity<Long> createGuest();
}
