package com.ssafy.ozz.clothes.clothes.service;

import com.ssafy.ozz.clothes.category.domain.CategoryLow;
import com.ssafy.ozz.clothes.category.service.CategoryService;
import com.ssafy.ozz.clothes.clothes.domain.Clothes;
import com.ssafy.ozz.clothes.clothes.dto.request.*;
import com.ssafy.ozz.clothes.clothes.dto.response.*;
import com.ssafy.ozz.clothes.clothes.repository.jpa.ClothesRepository;
import com.ssafy.ozz.clothes.coordinate.repository.jpa.CoordinateClothesRepository;
import com.ssafy.ozz.clothes.application.port.out.FilePort;
import com.ssafy.ozz.library.error.exception.ClothesNotFoundException;
import com.ssafy.ozz.library.error.exception.FileNotFoundException;
import com.ssafy.ozz.library.file.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import static com.ssafy.ozz.library.util.EnumBitwiseConverter.toBits;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClothesServiceImpl implements ClothesService {
    private static final byte DEMO_BATCH_DEFAULT_CATEGORY_LOW_ID = 1;

    private final ClothesRepository clothesRepository;
    private final CategoryService categoryService;
    private final FilePort filePort;
    private final WebClient webClient;
    private final MqService mqService;
    private final CoordinateClothesRepository coordinateClothesRepository;

    @Override
    @Transactional(readOnly = true)
    public Clothes getClothes(Long clothesId) {
        return clothesRepository.findById(clothesId).orElseThrow(ClothesNotFoundException::new);
    }

    @Override
    public ClothesWithFileResponse getClothesWithFile(Long clothesId){
        Clothes clothes = getClothes(clothesId);
        return new ClothesWithFileResponse(clothes, getFileInfoOrNull(clothes.getImageFileId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ClothesBasicWithFileResponse> getClothesOfUserWithFile(Long userId, ClothesSearchCondition condition, Pageable pageable) {
        if(condition.keyword() == null || condition.keyword().isEmpty()){
            return clothesRepository.findByUserId(userId, condition, pageable).map(this::toClothesBasicWithFileResponse);
        }else{
            return clothesRepository.findByUserId(userId, condition, pageable).map(this::toClothesBasicWithFileResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<Clothes> getClothesOfUser(Long userId, ClothesSearchCondition condition, Pageable pageable) {
        return clothesRepository.findByUserId(userId, condition, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClothesForRecommendationResponse> getClothesOfUser(Long userId) {
        return clothesRepository.findAllByUserIdAndProcessingLessThanEqual(userId,0).stream().map(clothes -> {
            FileInfo fileInfo = filePort.getFile(clothes.getImageFileId()).orElseThrow(FileNotFoundException::new);
            return new ClothesForRecommendationResponse(clothes, fileInfo);
        }).toList();
    }

    @Override
    public Clothes saveClothes(Long userId, MultipartFile imageFile, ClothesCreateRequest request) {
        CategoryLow categoryLow = categoryService.getCategoryLow(request.categoryLowId());
        FileInfo fileInfo = filePort.uploadFile(imageFile).orElseThrow(FileNotFoundException::new);
        Long imageFileId = fileInfo.fileId();
        Clothes clothes = clothesRepository.save(request.toEntity(categoryLow,imageFileId,userId));
        return clothes;
    }

    @Override
    public Clothes updateClothes(Long clothesId, ClothesUpdateRequest request) {
        Clothes clothes = getClothes(clothesId);
        if(request.categoryLowId() != null)
            clothes.changeCategoryLow(categoryService.getCategoryLow(request.categoryLowId()));
        clothes.changeName(request.name());
        clothes.changeSize(request.size());
        clothes.changeFit(request.fit());
        clothes.changeMemo(request.memo());
        clothes.changeBrand(request.brand());
        clothes.changePurchaseDate(request.purchaseDate());
        clothes.changePurchaseSite(request.purchaseSite());
        clothes.changeColor(toBits(request.colorList()));
        clothes.changeTexture(toBits(request.textureList()));
        clothes.changeSeason(toBits(request.seasonList()));
        clothes.changeStyle(toBits(request.styleList()));
        clothes.changePattern(toBits(request.patternList()));
        clothes.changeExtra(request.extra());
        clothes.updateProcessing(request.processing());
        clothes.changeUpdatedDate();

        return clothes;
    }

    @Override
    public ClothesWithFileResponse updateClothes(Long clothesId, ClothesUpdateRequest request, MultipartFile imageFile) {
        Clothes clothes = updateClothes(clothesId, request);
        FileInfo fileInfo = null;
        if(imageFile != null){
            // 이미지 파일 수정
            fileInfo = filePort.uploadFile(imageFile).orElseThrow();
            clothes.updateImageFile(fileInfo.fileId());
        } else {
            fileInfo = getFileInfoOrNull(clothes.getImageFileId());
        }

        return new ClothesWithFileResponse(clothes, fileInfo);
    }

    @Override
    public Long updateClothes(Long clothesId, MultipartFile imageFile) {
        Clothes clothes = getClothes(clothesId);
        FileInfo fileInfo = filePort.uploadFile(imageFile).orElseThrow();
        clothes.updateImageFile(fileInfo.fileId());
        clothes.updateProcessing(-1);

        return clothesId;
    }

    @Override
    public void deleteClothes(Long clothesId) {
        coordinateClothesRepository.deleteAllByClothes_ClothesId(clothesId);
        clothesRepository.deleteById(clothesId);
    }

    @Override
    public void deleteClothesList(List<Long> clothesIdList){
        clothesIdList.forEach(this::deleteClothes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clothes> getClothesInCoordinate(Long coordinateId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clothes> getClothesInRecCoordinate(Long coordinateId) {
        return List.of();
    }

    @Override
    public Flux<ServerSentEvent<String>> batchRegisterPurchaseHistory(Long userId, List<PurchaseHistory> purchaseHistories) {
        int totalItems = purchaseHistories.size();
        final int batchSize = 10;

        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        webClient.post()
                .uri("/api/v1/purchase-history/normalize")
                .bodyValue(purchaseHistories)
                .retrieve()
                .bodyToFlux(NormalizedResponse.class)
                .flatMap(response->{
                    int index= response.index()*batchSize;
                    List<ExtractAttribute> extractAttributes = new ArrayList<>();
                    CategoryLow defaultCategoryLow = categoryService.getCategoryLow(DEMO_BATCH_DEFAULT_CATEGORY_LOW_ID);
                    for (NormalizedItem item : response.data()) {
                        if(item.category()!=null){
                            PurchaseHistory purchaseHistory=purchaseHistories.get(index);
                            Clothes normalizedHistory = purchaseHistory.toEntity(userId,item.name(), defaultCategoryLow);
                            Long clothId = clothesRepository.save(normalizedHistory).getClothesId();
                            ExtractAttribute temp=item.toExtractAttribute(clothId, purchaseHistory.imgUrl());
                            extractAttributes.add(temp);
                        }
                        index++;
                    }
                    mqService.send(extractAttributes);
                    System.out.println(extractAttributes);
                    return Mono.just(100*((response.index()+1)/(Math.ceil(totalItems/(double)batchSize))));
                })
                .doOnNext(progress -> {
                    sink.tryEmitNext(ServerSentEvent.builder((int)Math.ceil(progress)+"%").build());
                })
                .doOnComplete(sink::tryEmitComplete)
                .doOnError(error -> {
                    log.error("구매내역 배치 등록 중 오류", error);
                    sink.tryEmitError(error);
                })
                .subscribe(progress -> {}, error -> {});

        return sink.asFlux();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ClothesBasicWithFileResponse> searchClothes(ClothesSearchCondition condition, Pageable pageable) {
        return clothesRepository.findByUserId(null, condition, pageable).map(this::toClothesBasicWithFileResponse);
    }

    private ClothesBasicWithFileResponse toClothesBasicWithFileResponse(Clothes clothes) {
        return new ClothesBasicWithFileResponse(clothes, getFileInfoOrNull(clothes.getImageFileId()));
    }

    private FileInfo getFileInfoOrNull(Long imageFileId) {
        if (imageFileId == null || imageFileId <= 0) {
            return null;
        }
        return filePort.getFile(imageFileId).orElse(null);
    }
}
