package com.ssafy.ozz.board.application.port.out;

import com.ssafy.ozz.library.user.UserInfo;

import java.util.Optional;

public interface UserPort {
    Optional<UserInfo> getUserInfo(Long userId);
    Optional<UserInfo> getUserInfoFromId(Long userId);
}
