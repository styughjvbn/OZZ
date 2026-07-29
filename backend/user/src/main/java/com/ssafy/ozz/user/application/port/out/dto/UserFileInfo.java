package com.ssafy.ozz.user.application.port.out.dto;

public record UserFileInfo(
        Long fileId,
        String filePath,
        String fileName,
        String fileType
){
}
