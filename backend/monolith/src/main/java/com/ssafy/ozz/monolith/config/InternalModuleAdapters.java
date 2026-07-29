package com.ssafy.ozz.monolith.config;

import com.ssafy.ozz.auth.global.service.RefreshService;
import com.ssafy.ozz.clothes.coordinate.service.CoordinateService;
import com.ssafy.ozz.fileserver.file.dto.response.FileInfoResponse;
import com.ssafy.ozz.fileserver.file.service.FileService;
import com.ssafy.ozz.library.file.FileInfo;
import com.ssafy.ozz.library.user.UserInfo;
import com.ssafy.ozz.user.domain.User;
import com.ssafy.ozz.user.application.port.out.dto.UserFileInfo;
import com.ssafy.ozz.user.service.GuestService;
import com.ssafy.ozz.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * 도메인 모듈의 outbound port를 다른 모듈의 application service에 연결한다.
 */
@Configuration
public class InternalModuleAdapters {

    @Bean
    com.ssafy.ozz.auth.application.port.out.UserAccountPort authUserAccountPort(GuestService guestService) {
        return guestService::createGuest;
    }

    @Bean
    com.ssafy.ozz.user.application.port.out.AuthTokenPort userAuthTokenPort(RefreshService refreshService) {
        return refreshService::deleteExistingRefreshToken;
    }

    @Bean
    com.ssafy.ozz.user.application.port.out.FilePort userFilePort(FileService fileService) {
        return new com.ssafy.ozz.user.application.port.out.FilePort() {
            @Override
            public Optional<UserFileInfo> uploadFile(MultipartFile file) {
                return Optional.of(toUserFile(save(fileService, file)));
            }

            @Override
            public Optional<UserFileInfo> getFile(Long fileId) {
                return Optional.of(toUserFile(fileService.getFile(fileId)));
            }
        };
    }

    @Bean
    com.ssafy.ozz.clothes.application.port.out.FilePort clothesFilePort(FileService fileService) {
        return new com.ssafy.ozz.clothes.application.port.out.FilePort() {
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
    com.ssafy.ozz.board.application.port.out.FilePort boardFilePort(FileService fileService) {
        return new com.ssafy.ozz.board.application.port.out.FilePort() {
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
    com.ssafy.ozz.board.application.port.out.UserPort boardUserPort(UserService userService) {
        return new com.ssafy.ozz.board.application.port.out.UserPort() {
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
    com.ssafy.ozz.favorite.application.port.out.CoordinatePort favoriteCoordinatePort(
            CoordinateService coordinateService
    ) {
        return new com.ssafy.ozz.favorite.application.port.out.CoordinatePort() {
            @Override
            public Optional<com.ssafy.ozz.favorite.application.port.out.dto.CoordinateInfo> getCoordinate(
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

    private static UserFileInfo toUserFile(FileInfoResponse file) {
        return new UserFileInfo((long) file.fileId(), file.filePath(), file.fileName(), file.fileType());
    }

    private static UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getNickname(), user.getProfileFileId(), user.getBirth());
    }

    private static com.ssafy.ozz.favorite.application.port.out.dto.CoordinateInfo toFavoriteCoordinate(
            com.ssafy.ozz.clothes.coordinate.dto.response.CoordinateBasicResponse coordinate
    ) {
        return new com.ssafy.ozz.favorite.application.port.out.dto.CoordinateInfo(
                coordinate.coordinateId(),
                coordinate.name(),
                coordinate.styleList(),
                coordinate.createdDate(),
                coordinate.imageFile()
        );
    }
}
