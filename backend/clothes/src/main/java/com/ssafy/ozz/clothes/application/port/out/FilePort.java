package com.ssafy.ozz.clothes.application.port.out;

import com.ssafy.ozz.library.file.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FilePort {
    Optional<FileInfo> uploadFile(MultipartFile file);
    Optional<FileInfo> getFile(Long fileId);
}
