package com.ssafy.ozz.user.application.port.out;

public interface AuthTokenPort {
    void deleteRefreshTokenOfUser(Long userId);
}
