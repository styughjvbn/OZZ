package com.ssafy.ozz.user.application.port.out;

import com.ssafy.ozz.user.application.port.out.dto.UserFileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FilePort {
    Optional<UserFileInfo> uploadFile(MultipartFile file);
    Optional<UserFileInfo> getFile(Long fileId);
}
