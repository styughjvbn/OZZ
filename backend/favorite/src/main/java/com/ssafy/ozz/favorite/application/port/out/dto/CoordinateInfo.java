package com.ssafy.ozz.favorite.application.port.out.dto;

import com.ssafy.ozz.library.clothes.properties.Style;
import com.ssafy.ozz.library.file.FileInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CoordinateInfo(
        Long coordinateId,
        String name,
        List<Style> styleList,
        LocalDateTime createdDate,
        FileInfo imageFile
){
}
