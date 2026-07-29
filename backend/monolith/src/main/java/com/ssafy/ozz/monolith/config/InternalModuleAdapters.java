package com.ssafy.ozz.monolith.config;

import com.ssafy.ozz.auth.global.service.RefreshService;
import com.ssafy.ozz.clothes.coordinate.dto.request.CoordinateSearchCondition;
import com.ssafy.ozz.clothes.coordinate.service.CoordinateService;
import com.ssafy.ozz.fileserver.file.dto.response.FileInfoResponse;
import com.ssafy.ozz.fileserver.file.service.FileService;
import com.ssafy.ozz.library.file.FileInfo;
import com.ssafy.ozz.library.user.UserInfo;
import com.ssafy.ozz.user.domain.User;
import com.ssafy.ozz.user.global.file.dto.FeignFileInfo;
import com.ssafy.ozz.user.service.GuestService;
import com.ssafy.ozz.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * 기존 Feign 계약을 보존하면서 호출을 프로세스 내부 메서드 호출로 바꾸는 전환 어댑터.
 * 각 도메인이 공개 application API를 갖추면 이 어댑터와 Feign 계약을 함께 제거한다.
 */
@Configuration
public class InternalModuleAdapters {

    @Bean
    com.ssafy.ozz.auth.global.util.UserClient authUserClient(GuestService guestService) {
        return () -> ResponseEntity.ok(guestService.createGuest());
    }

    @Bean
    com.ssafy.ozz.user.global.auth.AuthClient userAuthClient(RefreshService refreshService) {
        return userId -> {
            refreshService.deleteExistingRefreshToken(userId);
            return ResponseEntity.noContent().build();
        };
    }

    @Bean
    com.ssafy.ozz.user.global.file.FileClient userFileClient(FileService fileService) {
        return new com.ssafy.ozz.user.global.file.FileClient() {
            @Override
            public Optional<FeignFileInfo> uploadFile(MultipartFile file) {
                return Optional.of(toUserFile(save(fileService, file)));
            }

            @Override
            public Optional<FeignFileInfo> getFile(Long fileId) {
                return Optional.of(toUserFile(fileService.getFile(fileId)));
            }
        };
    }

    @Bean
    com.ssafy.ozz.clothes.global.fegin.file.FileClient clothesFileClient(FileService fileService) {
        return new com.ssafy.ozz.clothes.global.fegin.file.FileClient() {
            @Override
            public Optional<FileInfo> uploadFile(MultipartFile file) {
                return Optional.of(toFileInfo(save(fileService, file)));
            }

            @Override
            public Optional<FileInfo> getFile(Long fileId) {
                return Optional.of(toFileInfo(fileService.getFile(fileId)));
            }
        };
    }

    @Bean
    com.ssafy.ozz.board.global.feign.file.FileClient boardFileClient(FileService fileService) {
        return new com.ssafy.ozz.board.global.feign.file.FileClient() {
            @Override
            public Optional<FileInfo> uploadFile(MultipartFile file) {
                return Optional.of(toFileInfo(save(fileService, file)));
            }

            @Override
            public Optional<FileInfo> getFile(Long fileId) {
                if (fileId == null) {
                    return Optional.empty();
                }
                return Optional.of(toFileInfo(fileService.getFile(fileId)));
            }
        };
    }

    @Bean
    com.ssafy.ozz.board.global.feign.user.UserClient boardUserClient(UserService userService) {
        return new com.ssafy.ozz.board.global.feign.user.UserClient() {
            @Override
            public Optional<UserInfo> getUserInfo(Long userId) {
                return userService.getUserById(userId).map(InternalModuleAdapters::toUserInfo);
            }

            @Override
            public Optional<UserInfo> getUserInfoFromId(Long userId) {
                return userService.getUserById(userId).map(InternalModuleAdapters::toUserInfo);
            }
        };
    }

    @Bean
    com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateClient favoriteCoordinateClient(
            CoordinateService coordinateService
    ) {
        return new com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateClient() {
            @Override
            public Optional<org.springframework.data.domain.Slice<com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateBasicResponse>>
            getCoordinateList(Long userId, Long favoriteGroupId, Pageable pageable) {
                CoordinateSearchCondition condition = CoordinateSearchCondition.builder().build();
                return Optional.of(coordinateService.getCoordinatesOfUser(userId, condition, pageable)
                        .map(InternalModuleAdapters::toFavoriteCoordinate));
            }

            @Override
            public Optional<com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateBasicResponse> getCoordinate(
                    Long coordinateId
            ) {
                return Optional.of(toFavoriteCoordinate(
                        coordinateService.getCoordinateBasicResponse(coordinateId)
                ));
            }
        };
    }

    private static FileInfoResponse save(FileService fileService, MultipartFile file) {
        try {
            return fileService.saveFile(file);
        } catch (Exception exception) {
            throw new IllegalStateException("파일 모듈 호출에 실패했습니다.", exception);
        }
    }

    private static FileInfo toFileInfo(FileInfoResponse file) {
        return new FileInfo((long) file.fileId(), file.filePath(), file.fileName(), file.fileType());
    }

    private static FeignFileInfo toUserFile(FileInfoResponse file) {
        return new FeignFileInfo((long) file.fileId(), file.filePath(), file.fileName(), file.fileType());
    }

    private static UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getNickname(), user.getProfileFileId(), user.getBirth());
    }

    private static com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateBasicResponse toFavoriteCoordinate(
            com.ssafy.ozz.clothes.coordinate.dto.response.CoordinateBasicResponse coordinate
    ) {
        return new com.ssafy.ozz.favorite.global.feign.coordinate.CoordinateBasicResponse(
                coordinate.coordinateId(),
                coordinate.name(),
                coordinate.styleList(),
                coordinate.createdDate(),
                coordinate.imageFile()
        );
    }
}
