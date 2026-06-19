package com.ssafy.ozz.clothes.coordinate.repository.jpa;

import com.ssafy.ozz.clothes.coordinate.domain.Coordinate;
import com.ssafy.ozz.clothes.coordinate.repository.querydsl.CoordinateQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, Long>, CoordinateQueryRepository {
    @Modifying
    @Query(value = "delete from favorite where coordinate_id = :coordinateId", nativeQuery = true)
    void deleteFavoritesByCoordinateId(@Param("coordinateId") Long coordinateId);
}
