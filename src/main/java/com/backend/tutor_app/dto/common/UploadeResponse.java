package com.backend.tutor_app.dto.common;

import lombok.*;
import java.time.LocalDateTime;


@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadeResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private LocalDateTime uploadedAt;

    public static UploadeResponse of(String fileName, String fileUrl, String fileType, long fileSize) {
        return UploadeResponse.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
