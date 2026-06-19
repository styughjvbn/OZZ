package com.ssafy.ozz.favorite.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder(toBuilder = true)
@Table(name = "favorite")
@IdClass(FavoriteId.class)
public class Favorite {
    @Id
    @ManyToOne
    @JoinColumn(name = "favorite_group_id", nullable = false)
    private FavoriteGroup favoriteGroup;

    @Id
    @Column(name = "coordinate_id")
    private Long coordinateId;
}
