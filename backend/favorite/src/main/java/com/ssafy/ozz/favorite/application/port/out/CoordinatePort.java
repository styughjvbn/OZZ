package com.ssafy.ozz.favorite.application.port.out;

import com.ssafy.ozz.favorite.application.port.out.dto.CoordinateInfo;

import java.util.Optional;

public interface CoordinatePort {
    Optional<CoordinateInfo> getCoordinate(Long coordinateId);
}
