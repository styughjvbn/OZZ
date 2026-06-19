package com.ssafy.ozz.clothes.clothes.dto.request;

import com.ssafy.ozz.clothes.clothes.domain.Clothes;
import com.ssafy.ozz.clothes.category.domain.CategoryLow;
import java.time.LocalDate;

public record PurchaseHistory (
        String name,
        String brand,
        LocalDate purchaseDate,
        String purchaseSite,
        String imgUrl,
        String option
) {
    public Clothes toEntity(Long userId, String normalizedName, CategoryLow categoryLow) {
        return Clothes.builder()
                .name(normalizedName)
                .brand(brand)
                .purchaseDate(purchaseDate)
                .purchaseSite(purchaseSite)
                .userId(userId)
                .imageFileId(0L)
                .categoryLow(categoryLow)
                .processing(3)
                .build();
    }
}
