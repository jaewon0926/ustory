package com.elice.ustory.domain.paper.dto;

import com.elice.ustory.domain.paper.entity.Address;
import com.elice.ustory.domain.paper.entity.Image;
import com.elice.ustory.domain.paper.entity.Paper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddPaperRequest {

    @Schema(description = "타이틀", example = "합정역 맛집 우규")
    private String title;

    @Schema(description = "썸네일 URL", example = "https://~~~~")
    private String thumbnailImage;

    private List<String> images;

    @Schema(description = "방문 날짜", example = "2024-05-23")
    private LocalDate visitedAt;


    @Schema(description = "다이어리 Id", example = "12345678")
    private Long diaryId;

    @Schema(description = "사용자 Id (토큰 사용할 때 사라질 예정)", example = "12345678")
    private Long memberId;


    @Schema(description = "도로 주소", example = "서울특별시 마포구 독막로3길 21")
    private String city;

    @Schema(description = "상호명", example = "우규")
    private String store;

    @Schema(description = "X좌표", example = "37.5494")
    private double coordinateX;

    @Schema(description = "Y좌표", example = "126.9169")
    private double coordinateY;

    public Paper toPageEntity() {
        return Paper.createBuilder()
                .title(this.title)
                .thumbnailImage(this.thumbnailImage)
                .visitedAt(this.visitedAt)
                .build();
    }

    public Address toAddressEntity() {
        return Address.createBuilder()
                .city(this.city)
                .store(this.store)
                .coordinateX(this.coordinateX)
                .coordinateY(this.coordinateY)
                .build();
    }

    public List<Image> toImagesEntity() {
        return images.stream()
                .map(Image::new)
                .collect(Collectors.toList());
    }
}
