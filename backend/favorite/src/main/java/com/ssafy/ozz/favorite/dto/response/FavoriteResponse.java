package com.ssafy.ozz.favorite.dto.response;

import com.ssafy.ozz.favorite.domain.Favorite;
import com.ssafy.ozz.favorite.application.port.out.dto.CoordinateInfo;
import lombok.Builder;

@Builder
public record FavoriteResponse(
        Long favoriteId,
        CoordinateInfo coordinate
) {
    public static FavoriteResponse of(Favorite favorite, CoordinateInfo coordinate) {
        return FavoriteResponse.builder()
                .favoriteId(favorite.getCoordinateId())
                .coordinate(coordinate)
                .build();
    }
}
